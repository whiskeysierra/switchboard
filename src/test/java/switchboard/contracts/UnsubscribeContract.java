package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.Key;
import switchboard.Switchboard;
import switchboard.TestTimeout;
import switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.CancellationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.atLeastOnce;
import static switchboard.SubscriptionMode.exactlyOnce;
import static switchboard.SubscriptionMode.never;

interface UnsubscribeContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldUnsubscribe() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            // expected to unsubscribe itself in 1 ns
            final var key = Key.of(String.class, "foo");
            unit.subscribe(key, never(), Duration.ofNanos(1)).get();

            unit.publish(message(key, "foo"));

            final String actual = unit.subscribe(key, atLeastOnce(), Duration.ofMinutes(1)).get();
            assertThat(actual, is("foo"));
        });
    }

    @Test
    default void cancellingFutureShouldUnsubscribe() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50));
        future.cancel(false);

        unit.publish(message(matchA(), messageA()));

        assertThrows(CancellationException.class, future::get);
    }

}
