package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.times;
import static org.zalando.switchboard.Timeout.within;

interface TimeoutContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldTellThatThirdMessageDidNotOccur() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));
            unit.send(message(messageA(), deliveryMode()));

            final var exception = assertThrows(TimeoutException.class, () ->
                    unit.receive(matchA(), times(3), within(1, NANOS)));

            assertThat(exception.getMessage(), is("Expected exactly 3 Message message(s), but got 2 in 1 nanoseconds"));
        });
    }

    @Test
    default void shouldTellThatThirdMessageDidNotOccurWhenPollingAsync() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.send(message(messageA(), deliveryMode()));
            unit.send(message(messageA(), deliveryMode()));

            final var exception = assertThrows(TimeoutException.class, () ->
                    unit.subscribe(matchA(), times(3)).get(1, NANOSECONDS));

            assertThat(exception.getMessage(), is("Expected exactly 3 Message message(s), but got 2 in 1 nanoseconds"));
        });
    }

}
