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
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.Timeout.within;

public interface AtLeastOnceContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldFailIfExpectedAtLeastOneButReceivedNone() {
        final Switchboard unit = Switchboard.create();

        final TimeoutException exception = expectThrows(TimeoutException.class, () -> {
            unit.receive("foo"::equals, atLeastOnce(), within(1, NANOS));
        });

        assertThat(exception.getMessage(), is("Expected at least one Object message(s), but got 0 in 1 nanoseconds"));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedExactlyOne() throws TimeoutException, InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, atLeastOnce(), within(1, NANOS));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedTwo() throws TimeoutException, InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));
        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, atLeastOnce(), within(1, NANOS));
    }

}
