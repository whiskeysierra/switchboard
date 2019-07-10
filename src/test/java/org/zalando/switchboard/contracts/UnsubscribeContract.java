package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.SubscriptionMode;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.never;

interface UnsubscribeContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldUnsubscribe() throws TimeoutException, InterruptedException {
        final var unit = Switchboard.create();

        // expected to unsubscribe itself in 1 ns

        unit.subscribe("foo"::equals, never()).get(1, NANOSECONDS);

        unit.publish(message("foo"));

        final String actual = unit.subscribe("foo"::equals, SubscriptionMode.<String>exactlyOnce()).get(1, NANOSECONDS);
        assertThat(actual, is("foo"));
    }

    @Test
    default void cancellingFutureShouldUnsubscribe() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        unit.publish(message(messageA()));

        assertThrows(TimeoutException.class, () ->
                future.get(1, NANOSECONDS));
    }

}
