package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.never;

interface NeverContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldNotFailIfExpectedNoneAndReceivedNone() throws InterruptedException, TimeoutException, ExecutionException {
        final var unit = Switchboard.create();


        unit.subscribe("foo"::equals, never()).get(1, NANOSECONDS);
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithTimeout() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));

        final var exception = assertThrows(IllegalStateException.class,
                () -> unit.subscribe("foo"::equals, never()).get(1, NANOSECONDS));

        assertThat(exception.getMessage(), is("Expected to receive Object message(s) not even once within 1 nanoseconds, but got 1"));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithoutTimeout() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));

        final var exception = assertThrows(IllegalStateException.class, () ->
                unit.subscribe("foo"::equals, never()).get());

        assertThat(exception.getMessage(), is("Expected to receive Object message(s) not even once, but got 1"));
    }

}
