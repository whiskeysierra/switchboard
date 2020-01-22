package switchboard.contracts;

import org.checkerframework.checker.units.qual.K;
import org.junit.jupiter.api.Test;
import switchboard.Deliverable;
import switchboard.Key;
import switchboard.SubscriptionMode;
import switchboard.traits.SubscriptionTrait;
import switchboard.Switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.exactlyOnce;

interface ExactlyOnceContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldFailIfExpectedOneButReceivedNone() {
        final var unit = Switchboard.create();

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(Key.of(String.class, "foo"), exactlyOnce(), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(TimeoutException.class)));
        assertThat(cause.getMessage(), is("Expected to receive exactly one message(s) within PT0.05S, but got 0"));
    }

    @Test
    default void shouldNotFailIfExpectedOneAndReceivedExactlyOne() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));

        unit.subscribe(key, exactlyOnce(), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldFailIfExpectedOneButReceivedTwo() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(key, exactlyOnce(), Duration.ofMillis(50)).get());

        final Throwable cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive exactly one message(s) within PT0.05S, but got 2"));
    }

}
