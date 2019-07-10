package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@AllArgsConstructor
final class DefaultSubscription<T, R> implements Subscription<T, R> {

    private enum State {
        WAITING, DONE, CANCELLED
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.WAITING);

    private final Specification<T> specification;
    private final SubscriptionMode<T, R> mode;
    private final Consumer<Subscription<T, R>> unregister;

    private final BlockingQueue<Deliverable<T>> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger delivered = new AtomicInteger();

    @Override
    public Class<T> getMessageType() {
        return specification.getMessageType();
    }

    @Override
    public boolean test(final T input) {
        return specification.test(input);
    }

    @Override
    public void deliver(final Deliverable<T> deliverable) {
        queue.add(deliverable);

        // TODO race condition between queue + increment?
        if (mode.isDone(delivered.incrementAndGet())) {
            finish(State.DONE);
        }
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
    public R get() throws InterruptedException {
        checkArgument(!mode.requiresTimeout(),
                "Mode %s requires a timeout", mode.getClass().getSimpleName());

        return await(Long.MAX_VALUE, DAYS, this::format);
    }

    @Override
    public R get(final long timeout, final TimeUnit timeoutUnit) throws InterruptedException, TimeoutException {
        try {
            return await(timeout, timeoutUnit, count ->
                    format(count, timeout, timeoutUnit));
        } catch (final TimedOutException e) {
            throw new TimeoutException(e.getMessage());
        }
    }

    private R await(final long timeout, final TimeUnit timeoutUnit, final Function<Integer, String> message)
            throws InterruptedException {

        try {
            final List<T> results = new ArrayList<>();
            var received = 0;

            final var deadline = System.nanoTime() + timeoutUnit.toNanos(timeout);

            while (!mode.isDone(received)) {
                final var deliverable = queue.poll(deadline - System.nanoTime(), NANOSECONDS);
                final var elapsed = deliverable == null;

                if (elapsed) {
                    if (mode.isSuccess(received)) {
                        break;
                    }

                    throw new TimedOutException(format(received, timeout, timeoutUnit));
                }

                deliverable.deliverTo(results);

                received++;
            }

            return verifyAndTransform(results, received, message);
        } finally {
            unregister();
        }
    }

    private static final class TimedOutException extends RuntimeException {
        TimedOutException(final String message) {
            super(message);
        }
    }

    private R verifyAndTransform(final List<T> results, final int received, final Function<Integer, String> message) {
        if (mode.isSuccess(received)) {
            return mode.collect(results);
        } else {
            throw new IllegalStateException(message.apply(received));
        }
    }

    private String format(final int received, final long timeout, final TimeUnit timeoutUnit) {
        final String unit = timeoutUnit.name().toLowerCase(Locale.ENGLISH);
        return String.format("Expected to receive %s message(s) %s within %d %s, but got %d",
                getMessageName(), mode, timeout, unit, received);
    }

    private String format(final int received) {
        return String.format("Expected to receive %s message(s) %s, but got %d",
                getMessageName(), mode, received);
    }

    private String getMessageName() {
        return specification.getMessageType().getSimpleName();
    }

}
