package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor
final class DefaultSwitchboard implements Switchboard {

    private final Queue<Answer> answers = new ConcurrentLinkedQueue<>();

    private final AnsweringMachine machine;

    @Override
    public <T> void send(final Deliverable<T> deliverable) {
        final var matches = find(deliverable);

        if (matches.isEmpty()) {
            machine.record(deliverable);
        } else {
            final var deliveryMode = deliverable.getDeliveryMode();
            deliverTo(deliveryMode.distribute(matches), deliverable);
        }
    }

    private <T, R> List<Answer<T, R>> find(final Deliverable<T> deliverable) {
        return answers.stream()
                .filter(input -> input.test(deliverable.getMessage()))
                .map(this::<T, R>cast)
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <T, R> Answer<T, R> cast(final Answer answer) {
        return answer;
    }

    private <T, R> void deliverTo(final List<Answer<T, R>> list, final Deliverable<T> deliverable) {
        for (final var answer : list) {
            answer.deliver(deliverable);
            log.info("Successfully matched message [{}] to [{}]", deliverable.getMessage(), answer);
        }
    }

    private <T, R> void unregister(final Answer<T, R> answer) {
        if (answers.remove(answer)) {
            log.trace("Unregistered [{}].", answer);
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
        final var answer = new Answer<>(subscription, mode, this::unregister);

        answers.add(answer);
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
