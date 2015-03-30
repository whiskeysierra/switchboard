package de.zalando.circuit;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.stream.Collectors.toList;

final class DefaultCircuit implements Circuit {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCircuit.class);

    private final Queue<Deliverable> pending = new ConcurrentLinkedQueue<>();
    private final Queue<Delivery> deliveries = new ConcurrentLinkedQueue<>();

    private final LockSupport lock = new LockSupport();

    @Override
    public <E, H> List<H> inspect(Class<E> eventType, Class<H> hintType) {
        return copyOf(deliveries)
                .stream()
                .filter(delivery -> eventType.isAssignableFrom(delivery.getEventType()))
                .map(delivery -> this.<H>cast(delivery.getHint()))
                .map(hint -> hint.filter(hintType::isInstance).orElse(null))
                .collect(toList());
    }
    
    @SuppressWarnings("unchecked")
    private <H> Optional<H> cast(Optional hint) {
        return hint;
    }

    private <E> List<Delivery<E, ?>> find(Deliverable<E> deliverable) {
        return deliveries.stream()
                .filter(input -> input.test(deliverable.getEvent()))
                .map(this::<E>cast)
                .collect(toList());
    }

    @Override
    public <E> void send(E event, Distribution distribution) {
        deliver(new QueuedEvent<>(event, distribution));
    }

    @Override
    public <E> void fail(E event, Distribution distribution, RuntimeException exception) {
        deliver(new QueuedError<>(event, distribution, exception));
    }

    private <E> void deliver(Deliverable<E> deliverable) {
        lock.transactional(() -> {
            final List<Delivery<E, ?>> matches = find(deliverable);
            
            if (matches.isEmpty()) {
                pending.add(deliverable);
            } else {
                final Distribution distribution = deliverable.getDistribution();
                deliverTo(distribution.distribute(matches), deliverable);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <E> Delivery<E, ?> cast(Delivery delivery) {
        return delivery;
    }

    private <E> void deliverTo(final List<Delivery<E, ?>> list, final Deliverable<E> deliverable) {
        for (Delivery<E, ?> delivery : list) {
            delivery.deliver(deliverable);
            LOG.info("Successfully matched event [{}] to [{}]", deliverable.getEvent(), delivery);
        }
    }

    <E> void unregister(final Delivery<E, ?> delivery) {
        if (deliveries.remove(delivery)) {
            LOG.trace("Unregistered [{}].", delivery);
        }
    }

    @Override
    public <E> E receive(Subscription<E, ?> subscription, long timeout, TimeUnit timeoutUnit) throws TimeoutException {
        return receive(subscription, 1, timeout, timeoutUnit).get(0);
    }

    @Override
    public <E> List<E> receive(Subscription<E, ?> subscription, int count, long timeout, TimeUnit timeoutUnit) 
            throws TimeoutException {
        try {
            return subscribe(subscription, count).get(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // TODO is throwing an exception here the right thing?
            throw new RuntimeException("Thread has been interrupted while waiting");
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(e.getCause());
            throw new IllegalStateException(e.getCause());
        }
    }

    @Override
    public <E> Future<E> subscribe(Subscription<E, ?> subscription) {
        return Futures.lazyTransform(subscribe(subscription, 1), this::first);
    }

    private <T> T first(List<T> input) {
        return input.isEmpty() ? null : input.get(0);
    }

    @Override
    public <E> Future<List<E>> subscribe(Subscription<E, ?> subscription, int count) {
        final Delivery<E, ?> delivery = new Delivery<>(subscription, count, this::unregister);

        registerForFutureEvents(delivery);
        tryDeliverUnhandledEvents(delivery);

        return delivery;
    }

    private <E> void registerForFutureEvents(final Delivery<E, ?> subscription) {
        lock.transactional(() -> {
            checkState(!deliveries.contains(subscription), "[%s] is already registered", subscription);
            deliveries.add(subscription);
            LOG.trace("Registered [{}]", subscription);
        });
    }

    private <E> void tryDeliverUnhandledEvents(final Delivery<E, ?> delivery) {
        while (!delivery.isDone()) {
            final Optional<Deliverable<E>> match = findAndRemove(delivery);

            if (match.isPresent()) {
                final Deliverable<E> deliverable = match.get();
                deliverable.sendTo(this);
                final E event = deliverable.getEvent();
                LOG.info("Successfully matched previously unhandled event [{}] to [{}]", event, delivery);
            } else {
                break;
            }
        }
    }

    private <E> Optional<Deliverable<E>> findAndRemove(final Delivery<E, ?> delivery) {
        return lock.transactional(() -> {
            final Optional<Deliverable<E>> first = pending.stream()
                    .filter(event -> delivery.test(event.getEvent()))
                    .map(this::<E>cast)
                    .findFirst();

            if (first.isPresent()) {
                pending.remove(first.get());
            }

            return first;
        });
    }
    
    @SuppressWarnings("unchecked")
    private <E> Deliverable<E> cast(Deliverable event) {
        return event;
    }

}
