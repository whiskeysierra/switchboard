package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

interface ExactlyOnceContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldFailIfExpectedOneButReceivedNone() {
        final var unit = Switchboard.create();

        final var exception = assertThrows(CompletionException.class,
                () -> unit.subscribe("foo"::equals, exactlyOnce(), Duration.ofMillis(50)).join());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(TimeoutException.class)));
        assertThat(cause.getMessage(), is("Expected to receive exactly one message(s) within PT0.05S, but got 0"));
    }

    @Test
    default void shouldNotFailIfExpectedOneAndReceivedExactlyOne() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, exactlyOnce(), Duration.ofMillis(50)).join();
    }

    @Test
    default void shouldFailIfExpectedOneButReceivedTwo() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));

        final var exception = assertThrows(CompletionException.class,
                () -> unit.subscribe("foo"::equals, exactlyOnce(), Duration.ofMillis(50)).join());

        final Throwable cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive exactly one message(s) within PT0.05S, but got 2"));
    }

}
