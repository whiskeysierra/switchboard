package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
final class DefaultSwitchboard implements Switchboard {

    private final Subscriptions subscriptions;
    private final AnsweringMachine machine;

    @Override
    public <T> void send(final Deliverable<T> deliverable) {
        final var message = deliverable.getMessage();
        final var matches = subscriptions.find(message);

        if (matches.isEmpty()) {
            machine.record(deliverable);
        } else {
            final var deliveryMode = deliverable.getDeliveryMode();
            deliverTo(deliveryMode.distribute(matches), deliverable);
        }
    }

    private <T, R> void deliverTo(final List<Subscription<T, R>> list, final Deliverable<T> deliverable) {
        for (final var answer : list) {
            answer.deliver(deliverable);
            log.info("Successfully matched message [{}] to [{}]", deliverable.getMessage(), answer);
        }
    }

    @Override
    public <T, R> R receive(final Specification<T> specification, final SubscriptionMode<T, R> mode,
            final Duration timeout) throws InterruptedException, TimeoutException, ExecutionException {

        final var future = subscribe(specification, mode);
        return mode.block(future, timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public <T, R> Subscription<T, R> subscribe(final Specification<T> specification, final SubscriptionMode<T, R> mode) {
        final var answer = new DefaultSubscription<>(specification, mode, subscriptions::unregister);

        subscriptions.register(answer);
        tryDeliverRecordedMessages(answer);

        return answer;
    }

    private <T, R> void tryDeliverRecordedMessages(final Subscription<T, R> subscription) {
        while (!subscription.isDone()) {
            final var match = machine.removeIf(subscription);
            match.ifPresent(this::send);

            if (match.isEmpty()) {
                return;
            }
        }
    }

}
