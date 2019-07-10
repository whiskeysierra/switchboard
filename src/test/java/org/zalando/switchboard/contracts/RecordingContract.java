package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

interface RecordingContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptions() throws InterruptedException, TimeoutException {
        final var unit = Switchboard.create();

        unit.publish(message(messageA()));
        unit.publish(message(messageA()));

        final var firstResult = unit.subscribe(matchA(), atLeastOnce());
        final var first = firstResult.get(1, TimeUnit.NANOSECONDS);

        final var secondResult = unit.subscribe(matchA(), atLeastOnce());
        final var second = secondResult.get(1, TimeUnit.NANOSECONDS);

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test
    default void shouldDeliverRecordedMessagesToConcurrentSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            final var first = firstResult.get(1, TimeUnit.NANOSECONDS);
            final var second = secondResult.get(1, TimeUnit.NANOSECONDS);

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var first = firstResult.get(1, NANOSECONDS);

            unit.publish(message(messageA()));

            final var secondResult = unit.subscribe(matchA(), atLeastOnce());
            final var second = secondResult.get(1, NANOSECONDS);

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var first = firstResult.get(1, NANOSECONDS);

            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            unit.publish(message(messageA()));

            final var second = secondResult.get(1, NANOSECONDS);

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToConcurrentSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            unit.publish(message(messageA()));

            final var first = firstResult.get(1, NANOSECONDS);
            final var second = secondResult.get(1, NANOSECONDS);

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

}
