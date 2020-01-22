package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.Deliverable;
import switchboard.SubscriptionMode;
import switchboard.TestTimeout;
import switchboard.traits.SubscriptionTrait;
import switchboard.Switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

interface TimeoutContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldTellThatThirdMessageDidNotOccur() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(Deliverable.message(matchA(), messageA()));
            unit.publish(Deliverable.message(matchA(), messageA()));

            final var exception = assertThrows(ExecutionException.class,
                    () -> unit.subscribe(matchA(), SubscriptionMode.times(3), Duration.ofMillis(50)).get());

            final var cause = exception.getCause();
            assertThat(cause, is(instanceOf(TimeoutException.class)));
            assertThat(cause.getMessage(), is("Expected to receive exactly 3 message(s) within PT0.05S, but got 2"));
        });
    }

    @Test
    default void shouldTellThatThirdMessageDidNotOccurWhenPollingAsync() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(Deliverable.message(matchA(), messageA()));
            unit.publish(Deliverable.message(matchA(), messageA()));

            final var exception = assertThrows(ExecutionException.class,
                    () -> unit.subscribe(matchA(), SubscriptionMode.times(3), Duration.ofMillis(50)).get());

            final var cause = exception.getCause();
            assertThat(cause, is(instanceOf(TimeoutException.class)));
            assertThat(cause.getMessage(), is("Expected to receive exactly 3 message(s) within PT0.05S, but got 2"));
        });
    }

}
