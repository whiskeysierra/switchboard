package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

interface RecordingContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptions() {
        final var unit = Switchboard.create();

        unit.publish(message(messageA()));
        unit.publish(message(messageA()));

        final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        final var first = firstResult.join();

        final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        final var second = secondResult.join();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test
    default void shouldDeliverRecordedMessagesToConcurrentSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            final var first = firstResult.join();
            final var second = secondResult.join();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var first = firstResult.join();

            unit.publish(message(messageA()));

            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var second = secondResult.join();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var first = firstResult.join();

            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(messageA()));

            final var second = secondResult.join();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToConcurrentSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(messageA()));

            final var first = firstResult.join();
            final var second = secondResult.join();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

}
