package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

interface AtLeastOnceContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldFailIfExpectedAtLeastOneButReceivedNone() {
        final var unit = Switchboard.create();

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe("foo"::equals, atLeastOnce(), Duration.ofMillis(50)).get());

        assertThat(exception.getCause().getMessage(), is("Expected to receive at least one message(s) within PT0.05S, but got 0"));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedExactlyOne() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atLeastOnce(), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedTwo() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        unit.publish(message("foo"));
        unit.publish(message("foo"));

        unit.subscribe("foo"::equals, atLeastOnce(), Duration.ofMillis(50)).get();
    }

}
