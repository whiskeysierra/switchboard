package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.TestTimeout;
import switchboard.Switchboard;
import switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.atLeastOnce;

interface RecordingContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptions() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        unit.publish(message(matchA(), messageA()));
        unit.publish(message(matchA(), messageA()));

        final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        final var first = firstResult.get();

        final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        final var second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test
    default void shouldDeliverRecordedMessagesToConcurrentSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(matchA(), messageA()));
            unit.publish(message(matchA(), messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(matchA(), messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var first = firstResult.get();

            unit.publish(message(matchA(), messageA()));

            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToSubscriptionsOneAtATime() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(matchA(), messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var first = firstResult.get();

            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(matchA(), messageA()));

            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldDeliverPartlyRecordedMessagesToConcurrentSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(matchA(), messageA()));

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(matchA(), messageA()));

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

}
