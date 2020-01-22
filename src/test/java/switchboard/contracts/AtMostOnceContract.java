package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.Key;
import switchboard.traits.SubscriptionTrait;
import switchboard.Switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.atMostOnce;

interface AtMostOnceContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldNotFailIfExpectedAtMostOnceButReceivedNone() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        final var result = unit.subscribe(key, atMostOnce(), Duration.ofMillis(50)).get();

        assertThat(result, is(empty()));
    }

    @Test
    default void shouldNotFailIfExpectedAtMostOnceAndReceivedExactlyOne() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));

        final var result = unit.subscribe(key, atMostOnce(), Duration.ofMillis(50)).get();

        assertThat(result.orElse(null), is("foo"));
    }

    @Test
    default void shouldFailIfExpectedAtMostOnceButReceivedTwoWithTimeout() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(key, atMostOnce(), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive at most one message(s) within PT0.05S, but got 2"));
    }

    @Test
    default void shouldFailIfExpectedAtMostOnceButReceivedTwoWithoutTimeout() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> (unit.subscribe(key, atMostOnce(), Duration.ofMillis(50))).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive at most one message(s) within PT0.05S, but got 2"));
    }

}
