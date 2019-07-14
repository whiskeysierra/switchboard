package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.never;

interface NeverContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldNotFailIfExpectedNoneAndReceivedNone() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        unit.subscribe("foo"::equals, never(), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithTimeout() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe("foo"::equals, never(), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive not even one message(s) within PT0.05S, but got 1"));
    }

}
