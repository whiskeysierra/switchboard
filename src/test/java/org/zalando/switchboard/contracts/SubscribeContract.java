package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.TimeoutException;

import static java.util.Collections.frequency;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeast;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.times;

interface SubscribeContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldDeliverMessageToSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            unit.publish(message(messageA()));

            final var secondResult = unit.subscribe(matchA(), atLeastOnce());
            unit.publish(message(messageA()));

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldSkipNonMatchingMessages() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());

            unit.publish(message(messageA()));
            unit.publish(message(messageB()));

            final var first = firstResult.get();
            assertThat(first, is(messageA()));
        });
    }

    @Test
    default void shouldTimeoutWhenThereAreNoMatchingMessages() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            assertThrows(TimeoutException.class, () -> unit.subscribe(matchB(), exactlyOnce()).get(1, NANOSECONDS));
        });
    }

    @Test
    default void shouldPollMultipleTimesWhenCountGiven() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            for (var i = 0; i < count; i++) {
                unit.publish(message(messageA()));
            }


            final var messages = unit.subscribe(matchA(), times(count)).get(1, NANOSECONDS);

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncMultipleTimesWhenCountGiven() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            final var future = unit.subscribe(matchA(), atLeast(count));

            for (var i = 0; i < count; i++) {
                unit.publish(message(messageA()));
            }

            final var messages = future.get();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncWithTimeoutMultipleTimesWhenCountGiven() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            final var future = unit.subscribe(matchA(), times(count));

            for (var i = 0; i < count; i++) {
                unit.publish(message(messageA()));
            }

            final var messages = future.get(1, NANOSECONDS);

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

}
