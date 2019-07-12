package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.TestTimeout;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.times;

interface TimeoutContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldTellThatThirdMessageDidNotOccur() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var exception = assertThrows(CompletionException.class,
                    () -> unit.subscribe(matchA(), times(3), Duration.ofMillis(50)).join());

            final var cause = exception.getCause();
            assertThat(cause, is(instanceOf(TimeoutException.class)));
            assertThat(cause.getMessage(), is("Expected to receive exactly 3 message(s) within PT0.05S, but got 2"));
        });
    }

    @Test
    default void shouldTellThatThirdMessageDidNotOccurWhenPollingAsync() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var exception = assertThrows(CompletionException.class, () ->
                    unit.subscribe(matchA(), times(3), Duration.ofMillis(50)).join());

            final var cause = exception.getCause();
            assertThat(cause, is(instanceOf(TimeoutException.class)));
            assertThat(cause.getMessage(), is("Expected to receive exactly 3 message(s) within PT0.05S, but got 2"));
        });
    }

}
