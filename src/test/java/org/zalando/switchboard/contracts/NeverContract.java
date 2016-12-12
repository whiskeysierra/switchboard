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
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.Timeout.within;

public interface NeverContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldNotFailIfExpectedNoneAndReceivedNone() throws InterruptedException, TimeoutException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.receive("foo"::equals, never(), within(1, NANOS));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithTimeout() {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                unit.receive("foo"::equals, never(), within(1, NANOS)));

        assertThat(exception.getMessage(), is("Expected no Object message(s), but got 1 in 1 nanoseconds"));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithoutTimeout() {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                unit.subscribe("foo"::equals, never()).get());

        assertThat(exception.getMessage(), is("Expected no Object message(s), but got 1"));
    }

}
