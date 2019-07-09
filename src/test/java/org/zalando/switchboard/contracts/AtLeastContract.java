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
import static org.zalando.switchboard.SubscriptionMode.atLeast;
import static org.zalando.switchboard.Timeout.within;

interface AtLeastContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldFailIfExpectedAtLeastThreeButReceivedOnlyTwo() {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final var exception = assertThrows(TimeoutException.class, () ->
                unit.receive("foo"::equals, atLeast(3), within(1, NANOS)));

        assertThat(exception.getMessage(), is("Expected at least 3 Object message(s), but got 2 in 1 nanoseconds"));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastThreeAndReceivedExactlyThree() throws TimeoutException, InterruptedException, ExecutionException {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, atLeast(3), within(1, NANOS));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastThreeAndReceivedFour() throws TimeoutException, InterruptedException, ExecutionException {
        final var unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, atLeast(3), within(1, NANOS));
    }

}
