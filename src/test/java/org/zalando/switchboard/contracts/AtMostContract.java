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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atMost;

interface AtMostContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldNotFailIfExpectedAtMostThreeButReceivedOnlyTwo() throws InterruptedException, TimeoutException {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atMost(3)).get(1, NANOSECONDS);
    }

    @Test
    default void shouldNotFailIfExpectedAtMostThreeAndReceivedExactlyThree() throws InterruptedException, TimeoutException {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atMost(3)).get(1, NANOSECONDS);
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithTimeout() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));

        final var exception = assertThrows(IllegalStateException.class,
                () -> unit.subscribe("foo"::equals, atMost(3)).get(1, NANOSECONDS));

        assertThat(exception.getMessage(), is("Expected to receive at most 3 message(s) within 1 nanoseconds, but got 4"));
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithout() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));

        final var exception = assertThrows(IllegalStateException.class, () ->
                unit.subscribe("foo"::equals, atMost(3)).get(1, TimeUnit.NANOSECONDS));

        assertThat(exception.getMessage(), is("Expected to receive at most 3 message(s) within 1 nanoseconds, but got 4"));
    }

}
