package org.zalando.switchboard.contracts;

import org.junit.gen5.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

public interface RecordContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptions() throws ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverRecordedMessagesToConcurrentSubscriptions() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        final S first = firstResult.get();
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverRecordedMessagesToSubscriptionsOneAtATime() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverPartlyRecordedMessagesToSubscriptionsOneAtATime() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(message(messageA(), deliveryMode()));

        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverPartlyRecordedMessagesToConcurrentSubscriptions() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(message(messageA(), deliveryMode()));

        final S first = firstResult.get();
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

}
