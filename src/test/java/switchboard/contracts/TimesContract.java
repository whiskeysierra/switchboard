package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.Key;
import switchboard.SubscriptionMode;
import switchboard.Switchboard;
import switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static switchboard.Deliverable.message;

interface TimesContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldFailIfExpectedThreeButReceivedOnlyTwo() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(key, SubscriptionMode.times(3), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(TimeoutException.class)));
        assertThat(cause.getMessage(), is("Expected to receive exactly 3 message(s) within PT0.05S, but got 2"));
    }

    @Test
    default void shouldNotFailIfExpectedThreeAndReceivedExactlyThree() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        unit.subscribe(key, SubscriptionMode.times(3), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldFailIfExpectedThreeButReceivedFour() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(key, SubscriptionMode.times(3), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive exactly 3 message(s) within PT0.05S, but got 4"));
    }

}
