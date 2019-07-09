package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
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
import static org.zalando.switchboard.Timeout.within;

public interface SubscribeContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldDeliverMessageToSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));
            final var firstResult = unit.subscribe(matchA(), atLeastOnce());

            unit.send(message(messageA(), deliveryMode()));
            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldTimeoutWhenThereAreNoMatchingMessages() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));
            unit.send(message(messageA(), deliveryMode()));

            assertThrows(TimeoutException.class, () ->
                    unit.receive(matchB(), exactlyOnce(), within(1, NANOS)));
        });
    }

    @Test
    default void shouldPollMultipleTimesWhenCountGiven() throws TimeoutException, InterruptedException, ExecutionException {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            for (var i = 0; i < count; i++) {
                unit.send(message(messageA(), deliveryMode()));
            }

            final var messages = unit.receive(matchA(), times(count), within(1, NANOS));

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
                unit.send(message(messageA(), deliveryMode()));
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
                unit.send(message(messageA(), deliveryMode()));
            }

            final var messages = future.get(1, NANOSECONDS);

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

}
