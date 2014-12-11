package de.zalando.circuit;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.util.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.compose;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static de.zalando.circuit.Locking.transactional;

final class DefaultCircuit implements Circuit {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCircuit.class);

    private final Queue<QueuedEvent> pending = new ConcurrentLinkedQueue<>();
    private final Queue<Delivery> deliveries = new ConcurrentLinkedQueue<>();

    private Lock lock = new ReentrantLock();

    @Override
    public <E, H> List<H> inspect(Class<E> eventType, Class<H> hintType) {
        return from(copyOf(deliveries))
                .filter(compose(isSubtypeOf(eventType), Delivery::getEventType))
                .filter(compose(isSubtypeOf(hintType), Delivery::getHintType))
                .transform(Delivery::getMetadata)
                .filter(hintType)
                .toList();
    }

    private <E> Predicate<Class> isSubtypeOf(final Class<E> type) {
        return type::isAssignableFrom;
    }

    @Override
    public <E> void send(E event, Distribution distribution) {
        transactional(lock, () -> {
            @SuppressWarnings("unchecked")
            final List<Delivery<E, ?>> matches = (List) from(deliveries).filter(appliesTo(event)).toList();

            if (matches.isEmpty()) {
                pending.add(new QueuedEvent<>(event, distribution));
            } else {
                deliverTo(distribution.distribute(matches), event);
            }
        });
    }

    private <E> Predicate<Delivery> appliesTo(final E event) {
        return input -> input.apply(event);
    }

    private <E> void deliverTo(final List<Delivery<E, ?>> list, final E event) {
        for (Delivery<E, ?> delivery : list) {
            delivery.deliver(event);
            LOG.info("Successfully matched event [{}] to [{}]", event, delivery);
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
    public <E> List<E> receive(Subscription<E, ?> subscription, int count, long timeout, TimeUnit timeoutUnit) throws TimeoutException {
        try {
            return subscribe(subscription, count).get(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // TODO is throwing an exception here the right thing?
            throw new RuntimeException("Thread has been interrupted while waiting");
        } catch (ExecutionException e) {
            // this can't happen since we never throw it
            throw new AssertionError(e);
        }
    }

    @Override
    public <E> Future<E> subscribe(Subscription<E, ?> subscription) {
        return Futures.lazyTransform(subscribe(subscription, 1), elementAt(0));
    }

    private <T> Function<List<T>, T> elementAt(final int index) {
        return input -> index >= input.size() ? null : input.get(index);
    }

    @Override
    public <E> Future<List<E>> subscribe(Subscription<E, ?> subscription, int count) {
        final Delivery<E, ?> delivery = new Delivery<>(subscription, count, this::unregister);

        registerForFutureEvents(delivery);
        tryDeliverUnhandledEvents(delivery);

        return delivery;
    }

    private <E> void registerForFutureEvents(final Delivery<E, ?> subscription) {
        transactional(lock, () -> {
            checkState(!deliveries.contains(subscription), "[%s] is already registered", subscription);
            deliveries.add(subscription);
            LOG.trace("Registered [{}]", subscription);
        });
    }

    private <E> void tryDeliverUnhandledEvents(final Delivery<E, ?> delivery) {
        while (!delivery.isDone()) {
            final Optional<QueuedEvent<E>> match = findAndRemove(delivery);

            if (match.isPresent()) {
                final QueuedEvent<E> event = match.get();
                event.deliverTo(this);
                LOG.info("Successfully matched previously unhandled event [{}] to [{}]", event, delivery);
            } else {
                break;
            }
        }
    }

    private <E> Optional<QueuedEvent<E>> findAndRemove(final Delivery<E, ?> delivery) {
        return transactional(lock, () -> {
            @SuppressWarnings("unchecked")
            final Optional<QueuedEvent<E>> first = (Optional) from(pending)
                    .firstMatch((compose(delivery, QueuedEvent::getOriginal)));

            if (first.isPresent()) {
                pending.remove(first.get());
            }

            return first;
        });
    }

}
