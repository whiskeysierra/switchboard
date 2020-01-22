package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.Key;
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
import static switchboard.SubscriptionMode.atLeast;

interface AtLeastContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldFailIfExpectedAtLeastThreeButReceivedOnlyTwo() {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(key, atLeast(3), Duration.ofMillis(50)).get());

        final var cause = exception.getCause();
        assertThat(cause, is(instanceOf(TimeoutException.class)));
        assertThat(cause.getMessage(), is("Expected to receive at least 3 message(s) within PT0.05S, but got 2"));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastThreeAndReceivedExactlyThree() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        unit.subscribe(key, atLeast(3), Duration.ofMillis(250)).get();
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastThreeAndReceivedFour() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));
        unit.publish(message(key, "foo"));

        unit.subscribe(key, atLeast(3), Duration.ofMillis(50)).get();
    }

}
