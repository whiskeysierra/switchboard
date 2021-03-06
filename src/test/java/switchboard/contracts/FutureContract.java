package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.traits.SubscriptionTrait;
import switchboard.Switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.atLeastOnce;
import static switchboard.SubscriptionMode.exactlyOnce;

interface FutureContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void successfulFutureShouldBeDone() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(matchA(), messageA()));

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void waitingFutureShouldNotBeDone() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

        assertThat(future.isDone(), is(false));
    }

    @Test
    default void successfulFutureShouldNotBeCancelled() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(matchA(), messageA()));

        assertThat(future.isCancelled(), is(false));
    }

    @Test
    default void cancelledFutureShouldBeDone() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50));
        future.cancel(false);

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void cancelledFutureShouldBeCancelled() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50));

        future.cancel(false);

        assertThat(future.isCancelled(), is(true));
    }

    @Test
    default void cancellingWaitingFutureShouldSucceed() {
        final var unit = Switchboard.create();

        assertThat((unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(50))).cancel(false), is(true));
    }

    @Test
    default void cancellingDoneFutureShouldNotSucceed() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(matchA(), messageA()));

        assertThat(future.isDone(), is(true));
        assertThat(future.cancel(true), is(false));
    }

    @Test
    default void blockingOnFutureTwiceShouldWork() throws ExecutionException, InterruptedException {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(matchA(), messageA()));

        final var first = future.get();
        final var second = future.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test
    default void blockingOnFutureWithTimeoutTwiceShouldWork() throws ExecutionException, InterruptedException, TimeoutException {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
        unit.publish(message(matchA(), messageA()));

        final var first = future.get(10, MILLISECONDS);
        final var second = future.get(10, MILLISECONDS);

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test
    default void shouldTimeoutWhenWaitingWithASmallerTimeout() {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(200));

        final var executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> unit.publish(message(matchA(), messageA())), 150, MILLISECONDS);

        assertThrows(TimeoutException.class, () ->
                future.get(100, MILLISECONDS));
    }

    @Test
    default void shouldReturnWhenWaitingWithABiggerTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

        final var executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> unit.publish(message(matchA(), messageA())), 25, MILLISECONDS);

        final var result = future.get(100, MILLISECONDS);

        assertThat(result, is(messageA()));
    }

    @Test
    default void shouldReturnWhenWaitedAndTimedOutWithASmallerTimeoutEarlier() throws InterruptedException, ExecutionException, TimeoutException {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(200));

        final var executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> unit.publish(message(matchA(), messageA())), 100, MILLISECONDS);

        assertThrows(TimeoutException.class, () ->
                future.get(50, MILLISECONDS));

        final var result = future.get(200, MILLISECONDS);

        assertThat(result, is(messageA()));
    }

    @Test
    default void shouldFailWhenWaitedAndTimedOutWithASmallerTimeoutEarlier() throws InterruptedException, ExecutionException, TimeoutException {
        final var unit = Switchboard.create();

        final var future = unit.subscribe(matchA(), exactlyOnce(), Duration.ofMillis(200));

        final var executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            unit.publish(message(matchA(), messageA()));
            unit.publish(message(matchA(), messageA()));
        }, 100, MILLISECONDS);

        assertThrows(TimeoutException.class, () ->
                future.get(50, MILLISECONDS));

        final var exception = assertThrows(ExecutionException.class, () ->
                future.get(200, MILLISECONDS));

        assertThat(exception.getCause(), is(instanceOf(IllegalStateException.class)));
    }

}
