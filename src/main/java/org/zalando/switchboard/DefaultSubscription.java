package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

@AllArgsConstructor
final class DefaultSubscription<T, R> implements Subscription<T, R> {

    private enum State {
        WAITING, DONE, CANCELLED
    }

    private final long start = System.nanoTime();
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

        if (mode.isDone(delivered.incrementAndGet())) {
            // TODO this should only unregister/DONE when timeout has passed
            finish(State.DONE);
        }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (state.get() == State.WAITING) {
            return finish(State.CANCELLED);
        }

        return false;
    }

    @Override
    public boolean isCancelled() {
        return state.get() == State.CANCELLED;
    }

    // TODO if future is done, then get shouldn't need to block
    @Override
    public boolean isDone() {
        return state.get() != State.WAITING;
    }

    @Override
    public R get(final long timeout, final TimeUnit timeoutUnit) throws InterruptedException, TimeoutException {
        try {
            final Blocker blocker = new Blocker(start, timeout, timeoutUnit);

            final List<T> results = new ArrayList<>();

            // TODO this is not threadsafe
            var received = 0;

            while (!mode.isDone(received)) {
                @Nullable final var deliverable = blocker.block();

                if (deliverable == null) {
                    if (mode.isSuccess(received)) {
                        break;
                    }

                    throw new TimeoutException(blocker.format(received));
                }

                deliverable.deliverTo(results::add);
                received++;
            }

            if (mode.isSuccess(received)) {
                return mode.collect(results);
            } else {
                throw new IllegalStateException(blocker.format(received));
            }
        } finally {
            finish(State.DONE);
        }
    }

    private final class Blocker {

        private final long deadline;
        private final long timeout;
        private final TimeUnit timeoutUnit;

        private Blocker(final long start, final long timeout, final TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            this.deadline = start + timeoutUnit.toNanos(timeout);
        }

        Deliverable<T> block() throws InterruptedException {
            return queue.poll(deadline - System.nanoTime(), NANOSECONDS);
        }

        private String format(final int received) {
            return String.format("Expected to receive %s message(s) within %d %s, but got %d",
                    mode, timeout, timeoutUnit.name().toLowerCase(Locale.ENGLISH), received);
        }

    }

    private boolean finish(final State endState) {
        unregister();
        return state.compareAndSet(State.WAITING, endState);
    }

    private void unregister() {
        unregister.accept(this);
    }

}
