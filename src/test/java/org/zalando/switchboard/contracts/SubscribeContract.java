package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.frequency;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeast;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.times;

interface SubscribeContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldDeliverMessageToSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            unit.publish(message(messageA()));

            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            unit.publish(message(messageA()));

            final var first = firstResult.join();
            final var second = secondResult.join();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldSkipNonMatchingMessages() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(messageA()));
            unit.publish(message(messageB()));

            final var first = firstResult.join();
            assertThat(first, is(messageA()));
        });
    }

    @Test
    default void shouldTimeoutWhenThereAreNoMatchingMessages() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var exception = assertThrows(CompletionException.class, () ->
                    unit.subscribe(matchB(), exactlyOnce(), Duration.ofMillis(50)).join());

            final var cause = exception.getCause();
            assertThat(cause, is(instanceOf(TimeoutException.class)));
        });
    }

    @Test
    default void shouldPollMultipleTimesWhenCountGiven() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            for (var i = 0; i < count; i++) {
                unit.publish(message(messageA()));
            }

            final var messages = unit.subscribe(matchA(), times(count), Duration.ofMillis(50)).join();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncMultipleTimesWhenCountGiven() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            final var future = unit.subscribe(matchA(), atLeast(count), Duration.ofMillis(50));

            for (var i = 0; i < count; i++) {
                unit.publish(message(messageA()));
            }

            final var messages = future.join();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncWithTimeoutMultipleTimesWhenCountGiven() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            final var future = unit.subscribe(matchA(), times(count), Duration.ofMillis(50));

            for (var i = 0; i < count; i++) {
                unit.publish(message(messageA()));
            }

            final var messages = future.join();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

}
