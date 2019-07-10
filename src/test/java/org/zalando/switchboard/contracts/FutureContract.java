package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

interface FutureContract<S> extends SubscriptionTrait<S> {

    @Test
    default void successfulFutureShouldBeDone() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce());
        unit.publish(message(messageA()));

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void successfulFutureShouldNotBeCancelled() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce());
        unit.publish(message(messageA()));

        assertThat(future.isCancelled(), is(false));
    }

    @Test
    default void cancelledFutureShouldBeDone() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void cancelledFutureShouldBeCancelled() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce());

        future.cancel(false);

        assertThat(future.isCancelled(), is(true));
    }

    @Test
    default void cancellingWaitingFutureShouldSucceed() {
        final var unit = Switchboard.create();

        assertThat(unit.subscribe(matchA(), exactlyOnce()).cancel(false), is(true));
    }

    @Test
    default void cancellingDoneFutureShouldNotSucceed() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce());
        unit.publish(message(messageA()));

        assertThat(future.isDone(), is(true));
        assertThat(future.cancel(true), is(false));
    }

    @Test
    default void cancellingCancelledFutureShouldFail() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        assertThat(future.cancel(false), is(false));
    }

    @Test
    default void blockingOnFutureTwiceShouldWork() throws InterruptedException, TimeoutException {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce());
        unit.publish(message(messageA()));

        // TODO for this to work the subscription can't use poll (or anything that removes the element from the queue)
        // TODO future.get(1, NANOSECONDS);
        final var result = future.get(1, NANOSECONDS);

        assertThat(result, is(messageA()));
    }

}
