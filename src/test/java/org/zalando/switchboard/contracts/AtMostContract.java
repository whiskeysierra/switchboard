package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atMost;
import static org.zalando.switchboard.Timeout.within;

interface AtMostContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldNotFailIfExpectedAtMostThreeButReceivedOnlyTwo() throws InterruptedException, TimeoutException, ExecutionException {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, atMost(3), within(10, NANOS));
    }

    @Test
    default void shouldNotFailIfExpectedAtMostThreeAndReceivedExactlyThree() throws InterruptedException, TimeoutException, ExecutionException {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, atMost(3), within(1, NANOS));
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithTimeout() {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final var exception = assertThrows(IllegalStateException.class, () ->
                unit.receive("foo"::equals, atMost(3), within(1, NANOS)));

        assertThat(exception.getMessage(), is("Expected at most 3 Object message(s), but got 4 in 1 nanoseconds"));
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithout() {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final var exception = assertThrows(IllegalStateException.class, () ->
                unit.subscribe("foo"::equals, atMost(3)).get(1, TimeUnit.NANOSECONDS));

        assertThat(exception.getMessage(), startsWith("Expected at most 3 Object message(s), but got 4"));
    }

}
