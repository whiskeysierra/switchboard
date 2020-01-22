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
import static switchboard.SubscriptionMode.atMost;

interface AtMostContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldNotFailIfExpectedAtMostThreeButReceivedOnlyTwo() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        unit.subscribe(key, atMost(3), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldNotFailIfExpectedAtMostThreeAndReceivedExactlyThree() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        unit.subscribe(key, atMost(3), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithTimeout() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(key, atMost(3), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive at most 3 message(s) within PT0.05S, but got 4"));
    }

    @Test
    default void shouldFailIfExpectedAtMostThreeButReceivedFourWithout() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> (unit.subscribe(key, atMost(3), Duration.ofMillis(50))).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(IllegalStateException.class)));
        assertThat(cause.getMessage(), is("Expected to receive at most 3 message(s) within PT0.05S, but got 4"));
    }

}
