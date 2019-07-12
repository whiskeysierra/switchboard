package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

interface FutureContract<S> extends SubscriptionTrait<S> {

    @Test
    default void successfulFutureShouldBeDone() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(messageA()));

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void successfulFutureShouldNotBeCancelled() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(messageA()));

        assertThat(future.isCancelled(), is(false));
    }

    @Test
    default void cancelledFutureShouldBeDone() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50));
        future.cancel(false);

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void cancelledFutureShouldBeCancelled() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50));

        future.cancel(false);

        assertThat(future.isCancelled(), is(true));
    }

    @Test
    default void cancellingWaitingFutureShouldSucceed() {
        final var unit = Switchboard.create();

        assertThat((unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50))).cancel(false), is(true));
    }

    @Test
    default void cancellingDoneFutureShouldNotSucceed() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(messageA()));

        assertThat(future.isDone(), is(true));
        assertThat(future.cancel(true), is(false));
    }

    @Test
    default void blockingOnFutureTwiceShouldWork() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(messageA()));

        future.join();
        final var result = future.join();

        assertThat(result, is(messageA()));
    }

}
