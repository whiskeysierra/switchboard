package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.times;

interface TimeoutContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldTellThatThirdMessageDidNotOccur() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var exception = assertThrows(TimeoutException.class,
                    () -> unit.subscribe(matchA(), times(3)).get(1, NANOSECONDS));

            assertThat(exception.getMessage(), is("Expected to receive exactly 3 message(s) within 1 nanoseconds, but got 2"));
        });
    }

    @Test
    default void shouldTellThatThirdMessageDidNotOccurWhenPollingAsync() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var exception = assertThrows(TimeoutException.class, () ->
                    unit.subscribe(matchA(), times(3)).get(1, NANOSECONDS));

            assertThat(exception.getMessage(), is("Expected to receive exactly 3 message(s) within 1 nanoseconds, but got 2"));
        });
    }

}
