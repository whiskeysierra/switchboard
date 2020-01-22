package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.Key;
import switchboard.traits.SubscriptionTrait;
import switchboard.Switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.never;

interface NeverContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldNotFailIfExpectedNoneAndReceivedNone() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.subscribe(key, never(), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithTimeout() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(key, never(), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive not even one message(s) within PT0.05S, but got 1"));
    }

}
