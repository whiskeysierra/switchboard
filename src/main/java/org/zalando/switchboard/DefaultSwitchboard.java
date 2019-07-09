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

    private final Recipients recipients;
    private final AnsweringMachine machine;

    @Override
    public <T> void send(final Deliverable<T> deliverable) {
        final var matches = recipients.find(deliverable.getMessage());

        if (matches.isEmpty()) {
            machine.record(deliverable);
        } else {
            final var deliveryMode = deliverable.getDeliveryMode();
            deliverTo(deliveryMode.distribute(matches), deliverable);
        }
    }

    private <T, R> void deliverTo(final List<Answer<T, R>> list, final Deliverable<T> deliverable) {
        for (final var answer : list) {
            answer.deliver(deliverable);
            log.info("Successfully matched message [{}] to [{}]", deliverable.getMessage(), answer);
        }
    }

    @Override
    public <T, R> R receive(final Subscription<T> subscription, final SubscriptionMode<T, R> mode,
            final Duration timeout)
            throws InterruptedException, TimeoutException, ExecutionException {
        final var future = subscribe(subscription, mode);
        return mode.block(future, timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public <T, R> Answer<T, R> subscribe(final Subscription<T> subscription, final SubscriptionMode<T, R> mode) {
        final var answer = new DefaultAnswer<>(subscription, mode, recipients::unregister);

        recipients.register(answer);
        tryDeliverRecordedMessages(answer);

        return answer;
    }

    private <T, R> void tryDeliverRecordedMessages(final Answer<T, R> answer) {
        while (!answer.isDone()) {
            final var match = machine.removeIf(answer);

            match.ifPresent(this::send);

            if (match.isEmpty()) {
                return;
            }
        }
    }

}
