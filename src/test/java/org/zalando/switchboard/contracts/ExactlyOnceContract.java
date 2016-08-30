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
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.within;

public interface ExactlyOnceContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldFailIfExpectedOneWithoutTimeout() throws ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        final IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> {
            unit.subscribe("foo"::equals, exactlyOnce()).get();
        });

        assertThat(exception.getMessage(), is("Mode ExactlyOnce requires a timeout"));
    }

    @Test
    default void shouldFailIfExpectedOneButReceivedNone() {
        final Switchboard unit = Switchboard.create();

        final TimeoutException exception = expectThrows(TimeoutException.class, () -> {
            unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        });

        assertThat(exception.getMessage(), is("Expected exactly one Object message(s), but got 0 in 1 nanoseconds"));
    }

    @Test
    default void shouldNotFailIfExpectedOneAndReceivedExactlyOne() throws TimeoutException, InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
    }

    @Test
    default void shouldFailIfExpectedOneButReceivedTwo() {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        final IllegalStateException exception = expectThrows(IllegalStateException.class, () -> {
            unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        });

        assertThat(exception.getMessage(), is("Expected exactly one Object message(s), but got 2 in 1 nanoseconds"));
    }

}
