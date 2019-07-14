package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Specification;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.CancellationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.never;

interface UnsubscribeContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldUnsubscribe() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            // expected to unsubscribe itself in 1 ns
            unit.subscribe("foo"::equals, never(), Duration.ofNanos(1)).get();

            unit.publish(message("foo"));

            final Specification<String> equals = "foo"::equals;
            final String actual = unit.subscribe(equals, atLeastOnce(), Duration.ofMinutes(1)).get();
            assertThat(actual, is("foo"));
        });
    }

    @Test
    default void cancellingFutureShouldUnsubscribe() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50));
        future.cancel(false);

        unit.publish(message(messageA()));

        assertThrows(CancellationException.class, () -> future.get());
    }

}
