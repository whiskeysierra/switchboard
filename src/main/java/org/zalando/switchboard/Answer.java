package org.zalando.switchboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

final class Answer<T, R> implements Future<R>, Predicate<Object> {

    enum State {
        WAITING, DONE, CANCELLED
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.WAITING);

    private final Subscription<T> subscription;
    private final SubscriptionMode<T, R> mode;
    private final Consumer<Answer<T, R>> unregister;

    private final BlockingQueue<Deliverable<T>> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger delivered = new AtomicInteger();

    private final LockSupport lock = new LockSupport();

    Answer(final Subscription<T> subscription, final SubscriptionMode<T, R> mode, final Consumer<Answer<T, R>> unregister) {
        this.subscription = subscription;
        this.mode = mode;
        this.unregister = unregister;
    }

    Class<T> getMessageType() {
        return subscription.getMessageType();
    }

    @Override
    public boolean test(@Nullable final Object input) {
        return subscription.getMessageType().isInstance(input) && subscription.test(cast(input));
    }

    @SuppressWarnings("unchecked")
    private T cast(final Object input) {
        return (T) input;
    }

    void deliver(final Deliverable<T> deliverable) {
        lock.transactional(() -> {
            queue.add(deliverable);

            if (mode.isDone(delivered.incrementAndGet())) {
                finish(State.DONE);
            }
        });
    }

    private boolean finish(final State endState) {
        unregister();
        return state.compareAndSet(State.WAITING, endState);
    }

    private void unregister() {
        unregister.accept(this);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        switch (state.get()) {

            case WAITING:
                return finish(State.CANCELLED);

            case DONE:
                return false;

            // CANCELLED is the only state that is left, but with a default case we wouldn't get the line covered
            default:
                return true;
        }
    }

    @Override
    public boolean isCancelled() {
        return state.get() == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state.get() != State.WAITING;
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        checkTimeoutRequirement();

        try {
            final List<T> results = new ArrayList<>();
            var received = 0;

            while (!mode.isDone(received)) {
                final var deliverable = queue.take();
                deliver(results, deliverable);
                received++;
            }

            return verifyAndTransform(results, received, this::message);
        } finally {
            unregister();
        }
    }

    private void checkTimeoutRequirement() {
        checkArgument(!mode.requiresTimeout(), "Mode %s requires a timeout", mode.getClass().getSimpleName());
    }

    @Override
    public R get(final long timeout, final TimeUnit timeoutUnit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            final List<T> results = new ArrayList<>();
            var received = 0;

            final var deadline = System.nanoTime() + timeoutUnit.toNanos(timeout);

            while (!mode.isDone(received)) {
                final var deliverable = queue.poll(deadline - System.nanoTime(), NANOSECONDS);
                final var timedOut = deliverable == null;

                if (timedOut) {
                    verifyTimeout(received, timeout, timeoutUnit);
                    break;
                }

                deliver(results, deliverable);

                received++;
            }

            return verifyAndTransform(results, received, count ->
                    message(count, timeout, timeoutUnit));
        } finally {
            unregister();
        }
    }

    private void deliver(final List<T> results, final Deliverable<T> deliverable) throws ExecutionException {
        deliverable.deliverTo(results);
    }

    private R verifyAndTransform(final List<T> results, final int received, final Function<Integer, String> message) {
        if (mode.isSuccess(received)) {
            return mode.collect(results);
        } else {
            throw new IllegalStateException(message.apply(received));
        }
    }

    private void verifyTimeout(final int received, final long timeout, final TimeUnit timeoutUnit) throws TimeoutException {
        if (!mode.isSuccess(received)) {
            throw new TimeoutException(message(received, timeout, timeoutUnit));
        }
    }

    private String message(final int received) {
        return format("Expected %s %s message(s), but got %d", mode, getMessageName(), received);
    }

    private String message(final int received, final long timeout, final TimeUnit timeoutUnit) {
        return format("Expected %s %s message(s), but got %d in %d %s", mode, getMessageName(), received, timeout, humanize(timeoutUnit));
    }

    private String getMessageName() {
        return subscription.getMessageType().getSimpleName();
    }

    private String humanize(final TimeUnit timeoutUnit) {
        return timeoutUnit.name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription);
    }

    @Override
    public boolean equals(@Nullable final Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof Answer) {
            final var other = (Answer) that;
            return Objects.equals(subscription, other.subscription);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return subscription.toString();
    }

}
