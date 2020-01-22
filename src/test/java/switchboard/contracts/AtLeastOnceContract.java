package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.Deliverable;
import switchboard.Key;
import switchboard.Switchboard;
import switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static switchboard.SubscriptionMode.atLeastOnce;

interface AtLeastOnceContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldFailIfExpectedAtLeastOneButReceivedNone() {
        final var unit = Switchboard.create();

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(Key.of(String.class, "foo"), atLeastOnce(), Duration.ofMillis(50)).get());

        assertThat(exception.getCause().getMessage(), is("Expected to receive at least one message(s) within PT0.05S, but got 0"));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedExactlyOne() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(Deliverable.message(key, "foo"));

        unit.subscribe(key, atLeastOnce(), Duration.ofMillis(50)).get();
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedTwo() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var key = Key.of(String.class, "foo");
        unit.publish(Deliverable.message(key, "foo"));
        unit.publish(Deliverable.message(key, "foo"));

        unit.subscribe(key, atLeastOnce(), Duration.ofMillis(50)).get();
    }

}
