package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.TestTimeout;
import switchboard.traits.SubscriptionTrait;
import switchboard.Switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.frequency;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.atLeast;
import static switchboard.SubscriptionMode.atLeastOnce;
import static switchboard.SubscriptionMode.exactlyOnce;
import static switchboard.SubscriptionMode.times;

interface SubscribeContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldDeliverMessageToSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            unit.publish(message(matchA(), messageA()));

            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            unit.publish(message(matchA(), messageA()));

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

    @Test
    default void shouldSkipNonMatchingMessages() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(matchA(), messageA()));
            unit.publish(message(matchB(), messageB()));

            final var first = firstResult.get();
            assertThat(first, is(messageA()));
        });
    }

    @Test
    default void shouldTimeoutWhenThereAreNoMatchingMessages() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            unit.publish(message(matchA(), messageA()));
            unit.publish(message(matchA(), messageA()));

            final var exception = assertThrows(ExecutionException.class,
                    () -> unit.subscribe(matchB(), exactlyOnce(), Duration.ofMillis(50)).get());

            final var cause = exception.getCause();
            assertThat(cause, is(instanceOf(TimeoutException.class)));
        });
    }

    @Test
    default void shouldPollMultipleTimesWhenCountGiven() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            for (var i = 0; i < count; i++) {
                unit.publish(message(matchA(), messageA()));
            }

            final var messages = unit.subscribe(matchA(), times(count), Duration.ofMillis(50)).get();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncMultipleTimesWhenCountGiven() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            final var future = unit.subscribe(matchA(), atLeast(count), Duration.ofMillis(50));

            for (var i = 0; i < count; i++) {
                unit.publish(message(matchA(), messageA()));
            }

            final var messages = future.get();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldPollAsyncWithTimeoutMultipleTimesWhenCountGiven() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var unit = Switchboard.create();

            final var count = 5;

            final var future = unit.subscribe(matchA(), times(count), Duration.ofMillis(50));

            for (var i = 0; i < count; i++) {
                unit.publish(message(matchA(), messageA()));
            }

            final var messages = future.get();

            assertThat(messages, hasSize(count));
            assertThat(frequency(messages, messageA()), is(count));
        });
    }

    @Test
    default void shouldStopEarly() {
        final var unit = Switchboard.create();

        final var executor = Executors.newSingleThreadScheduledExecutor();

        executor.schedule(() ->
                unit.publish(message(matchA(), messageA())), 200, MILLISECONDS);

        assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
            unit.subscribe(matchA(), atLeastOnce(), Duration.ofSeconds(1)).get();
        });
    }

    @Test
    default void shouldNotOverDeliver() {
        final var unit = Switchboard.create();

        unit.publish(message(matchA(), messageA()));
        unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50));

        unit.publish(message(matchA(), messageA()));
    }

}
