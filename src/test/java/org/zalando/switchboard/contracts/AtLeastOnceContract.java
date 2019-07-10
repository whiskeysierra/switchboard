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
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

interface AtLeastOnceContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldFailIfExpectedAtLeastOneButReceivedNone() {
        final var unit = Switchboard.create();

        final var exception = assertThrows(TimeoutException.class,
                () -> unit.subscribe("foo"::equals, atLeastOnce()).get(1, NANOSECONDS));

        assertThat(exception.getMessage(), is("Expected to receive at least one message(s) within 1 nanoseconds, but got 0"));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedExactlyOne() throws TimeoutException, InterruptedException {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atLeastOnce()).get(1, NANOSECONDS);
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedTwo() throws TimeoutException, InterruptedException {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atLeastOnce()).get(1, NANOSECONDS);
    }

}
