package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atMost;

interface AtMostContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldNotFailIfExpectedAtMostThreeButReceivedOnlyTwo() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atMost(3), Duration.ofMillis(50)).join();
    }

    @Test
    default void shouldNotFailIfExpectedAtMostThreeAndReceivedExactlyThree() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atMost(3), Duration.ofMillis(50)).join();
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithTimeout() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));

        final var exception = assertThrows(CompletionException.class,
                () -> unit.subscribe("foo"::equals, atMost(3), Duration.ofMillis(50)).join());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive at most 3 message(s) within PT0.05S, but got 4"));
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithout() {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));
        unit.publish(message("foo"));

        final var exception = assertThrows(CompletionException.class,
                () -> (unit.subscribe("foo"::equals, atMost(3), Duration.ofMillis(50))).join());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive at most 3 message(s) within PT0.05S, but got 4"));
    }

}
