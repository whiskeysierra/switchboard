package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Subscription;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
    default void shouldThrowWhenSubscribingTwice() {
        final Switchboard unit = Switchboard.create();

        final Subscription<S, ?> subscription = matchA();
        unit.subscribe(subscription, exactlyOnce());

        assertThrows(IllegalStateException.class, () ->
                unit.subscribe(subscription, exactlyOnce()));
    }
    
    @Test
    default void shouldDeliverMessageToSubscriptions() throws InterruptedException, ExecutionException {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final Switchboard unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));
            final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());

            unit.send(message(messageA(), deliveryMode()));
            final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

            final S first = firstResult.get();
            final S second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldTimeoutWhenThereAreNoMatchingMessages() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final Switchboard unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));
            unit.send(message(messageA(), deliveryMode()));

            assertThrows(TimeoutException.class, () ->
                    unit.receive(matchB(), exactlyOnce(), within(1, NANOS)));
        });
    }

    @Test
    default void shouldPollMultipleTimesWhenCountGiven() throws TimeoutException, InterruptedException, ExecutionException {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final Switchboard unit = Switchboard.create();

            final int count = 5;

            for (int i = 0; i < count; i++) {
                unit.send(message(messageA(), deliveryMode()));
            }

            final List<S> messages = unit.receive(matchA(), times(count), within(1, NANOS));

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncMultipleTimesWhenCountGiven() throws ExecutionException, InterruptedException {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final Switchboard unit = Switchboard.create();

            final int count = 5;

            final Future<List<S>> future = unit.subscribe(matchA(), atLeast(count));

            for (int i = 0; i < count; i++) {
                unit.send(message(messageA(), deliveryMode()));
            }

            final List<S> messages = future.get();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncWithTimeoutMultipleTimesWhenCountGiven() throws ExecutionException, InterruptedException, TimeoutException {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final Switchboard unit = Switchboard.create();

            final int count = 5;

            final Future<List<S>> future = unit.subscribe(matchA(), times(count));

            for (int i = 0; i < count; i++) {
                unit.send(message(messageA(), deliveryMode()));
            }

            final List<S> messages = future.get(1, NANOSECONDS);

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

}
