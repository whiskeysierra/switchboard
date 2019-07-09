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
import static org.zalando.switchboard.SubscriptionMode.times;

interface TimesContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldFailIfExpectedThreeWithoutTimeout() {
        final var unit = Switchboard.create();

        final var exception = assertThrows(IllegalArgumentException.class, () ->
                unit.subscribe("foo"::equals, times(3)).get());

        assertThat(exception.getMessage(), is("Mode Times requires a timeout"));
    }

    @Test
    default void shouldFailIfExpectedThreeButReceivedOnlyTwo() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));

        final var exception = assertThrows(TimeoutException.class,
                () -> unit.subscribe("foo"::equals, times(3)).get(1, NANOSECONDS));

        assertThat(exception.getMessage(), is("Expected to receive Object message(s) 3 times within 1 nanoseconds, but got 2"));
    }

    @Test
    default void shouldNotFailIfExpectedThreeAndReceivedExactlyThree() throws TimeoutException, InterruptedException, ExecutionException {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));


        unit.subscribe("foo"::equals, times(3)).get(1, NANOSECONDS);
    }

    @Test
    default void shouldFailIfExpectedThreeButReceivedFour() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));

        final var exception = assertThrows(IllegalStateException.class,
                () -> unit.subscribe("foo"::equals, times(3)).get(1, NANOSECONDS));

        assertThat(exception.getMessage(), is("Expected to receive Object message(s) 3 times within 1 nanoseconds, but got 4"));
    }

}
