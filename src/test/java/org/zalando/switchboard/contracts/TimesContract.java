package org.zalando.switchboard.contracts;

import org.junit.gen5.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.times;
import static org.zalando.switchboard.Timeout.within;

public interface TimesContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldFailIfExpectedThreeWithoutTimeout() throws ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> {
            unit.subscribe("foo"::equals, times(3)).get();
        });

        assertThat(exception.getMessage(), is("Mode Times requires a timeout"));
    }

    @Test
    default void shouldFailIfExpectedThreeButReceivedOnlyTwo() {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final TimeoutException exception = expectThrows(TimeoutException.class, () -> {
            unit.receive("foo"::equals, times(3), within(1, NANOS));
        });

        assertThat(exception.getMessage(), is("Expected exactly 3 Object message(s), but got 2 in 1 nanoseconds"));
    }

    @Test
    default void shouldNotFailIfExpectedThreeAndReceivedExactlyThree() throws TimeoutException, InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, times(3), within(1, NANOS));
    }

    @Test
    default void shouldFailIfExpectedThreeButReceivedFour() {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final IllegalStateException exception = expectThrows(IllegalStateException.class, () -> {
            unit.receive("foo"::equals, times(3), within(1, NANOS));
        });

        assertThat(exception.getMessage(), is("Expected exactly 3 Object message(s), but got 4 in 1 nanoseconds"));
    }

}
