package org.zalando.switchboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.stream.Collectors.toList;

final class DefaultSwitchboard implements Switchboard {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSwitchboard.class);

    private final Queue<Deliverable> recorded = new ConcurrentLinkedQueue<>();
    private final Queue<Answer> answers = new ConcurrentLinkedQueue<>();

    private final LockSupport lock = new LockSupport();

    private <T, R> List<Answer<T, R, ?>> find(final Deliverable<T> deliverable) {
        return answers.stream()
                .filter(input -> input.test(deliverable.getMessage()))
                .map(this::<T, R>cast)
                .collect(toList());
    }

    @Override
    public <T> void send(final Deliverable<T> deliverable) {
        deliver(deliverable);
    }

    private <T, R> void deliver(final Deliverable<T> deliverable) {
        lock.transactional(() -> {
            final List<Answer<T, R, ?>> matches = find(deliverable);

            if (matches.isEmpty()) {
                recorded.add(deliverable);
            } else {
                final DeliveryMode deliveryMode = deliverable.getDeliveryMode();
                deliverTo(deliveryMode.distribute(matches), deliverable);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T, R> Answer<T, R, ?> cast(final Answer answer) {
        return answer;
    }

    private <T, R> void deliverTo(final List<Answer<T, R, ?>> list, final Deliverable<T> deliverable) {
        for (final Answer<T, R, ?> answer : list) {
            answer.deliver(deliverable);
            LOG.info("Successfully matched message [{}] to [{}]", deliverable.getMessage(), answer);
        }
    }

    private <T, R> void unregister(final Answer<T, R, ?> answer) {
        if (answers.remove(answer)) {
            LOG.trace("Unregistered [{}].", answer);
        }
    }

    @Override
    public <T, R> R receive(final Subscription<T> subscription, final SubscriptionMode<T, R> mode, final Duration timeout)
            throws InterruptedException, TimeoutException, ExecutionException {
        final Answer<T, R, ?> future = subscribe(subscription, mode);
        return mode.block(future, timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public <T, R> Answer<T, R, ?> subscribe(final Subscription<T> subscription, final SubscriptionMode<T, R> mode) {
        final Answer<T, R, ?> answer = new Answer<>(subscription, mode, this::unregister);

        registerForFutureMessages(answer);
        tryDeliverRecordedMessages(answer);

        return answer;
    }

    private <T, R> void registerForFutureMessages(final Answer<T, R, ?> answer) {
        lock.transactional(() -> {
            checkState(!answers.contains(answer), "[%s] is already registered", answer);
            answers.add(answer);
            LOG.trace("Registered [{}]", answer);
        });
    }

    private <T, R> void tryDeliverRecordedMessages(final Answer<T, R, ?> answer) {
        while (!answer.isDone()) {
            final Optional<Deliverable<T>> match = findAndRemove(answer);

            if (match.isPresent()) {
                final Deliverable<T> deliverable = match.get();
                send(deliverable);
                final T message = deliverable.getMessage();
                LOG.info("Successfully matched previously unhandled message [{}] to [{}]", message, answer);
            } else {
                break;
            }
        }
    }

    private <T, R> Optional<Deliverable<T>> findAndRemove(final Answer<T, R, ?> answer) {
        return lock.transactional(() -> {
            final Optional<Deliverable<T>> first = recorded.stream()
                    .filter(deliverable -> answer.test(deliverable.getMessage()))
                    .map(this::<T>cast)
                    .findFirst();

            first.ifPresent(recorded::remove);

            return first;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Deliverable<T> cast(final Deliverable deliverable) {
        return deliverable;
    }

}
