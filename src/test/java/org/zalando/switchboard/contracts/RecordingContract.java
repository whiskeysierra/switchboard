package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

public interface RecordingContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptions() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        final var firstResult = unit.subscribe(matchA(), atLeastOnce());
        final var first = firstResult.get();

        final var secondResult = unit.subscribe(matchA(), atLeastOnce());
        final var second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test
    default void shouldDeliverRecordedMessagesToConcurrentSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));
            unit.send(message(messageA(), deliveryMode()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var first = firstResult.get();

            unit.send(message(messageA(), deliveryMode()));

            final var secondResult = unit.subscribe(matchA(), atLeastOnce());
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var first = firstResult.get();

            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            unit.send(message(messageA(), deliveryMode()));

            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToConcurrentSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            unit.send(message(messageA(), deliveryMode()));

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

}
