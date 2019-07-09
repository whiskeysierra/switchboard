package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.times;
import static org.zalando.switchboard.Timeout.within;

interface TimesContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

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

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final var exception = assertThrows(TimeoutException.class, () ->
                unit.receive("foo"::equals, times(3), within(1, NANOS)));

        assertThat(exception.getMessage(), is("Expected exactly 3 Object message(s), but got 2 in 1 nanoseconds"));
    }

    @Test
    default void shouldNotFailIfExpectedThreeAndReceivedExactlyThree() throws TimeoutException, InterruptedException, ExecutionException {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, times(3), within(1, NANOS));
    }

    @Test
    default void shouldFailIfExpectedThreeButReceivedFour() {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final var exception = assertThrows(IllegalStateException.class, () ->
                unit.receive("foo"::equals, times(3), within(1, NANOS)));

        assertThat(exception.getMessage(), is("Expected exactly 3 Object message(s), but got 4 in 1 nanoseconds"));
    }

}
