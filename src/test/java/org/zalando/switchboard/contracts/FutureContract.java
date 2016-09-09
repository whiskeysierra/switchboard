package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

public interface FutureContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void successfulFutureShouldBeDone() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), atLeastOnce());
        unit.send(message(messageA(), deliveryMode()));

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void successfulFutureShouldNotBeCancelled() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), atLeastOnce());
        unit.send(message(messageA(), deliveryMode()));

        assertThat(future.isCancelled(), is(false));
    }

    @Test
    default void cancelledFutureShouldBeDone() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void cancelledFutureShouldBeCancelled() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());

        future.cancel(false);

        assertThat(future.isCancelled(), is(true));
    }

    @Test
    default void cancellingWaitingFutureShouldSucceed() {
        final Switchboard unit = Switchboard.create();

        assertThat(unit.subscribe(matchA(), exactlyOnce()).cancel(false), is(true));
    }

    @Test
    default void cancellingDoneFutureShouldNotSucceed() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), atLeastOnce());
        unit.send(message(messageA(), deliveryMode()));

        assertThat(future.cancel(true), is(false));
    }

    @Test
    default void cancellingCancelledFutureShouldSucceed() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        assertThat(future.cancel(false), is(true));
    }

}
