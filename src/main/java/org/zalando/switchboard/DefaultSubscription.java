package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import org.organicdesign.fp.collections.ImList;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static java.util.Collections.unmodifiableCollection;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.organicdesign.fp.StaticImports.vec;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@AllArgsConstructor
final class DefaultSubscription<T, R> implements Subscription<T>, Promise<R> {

    private final Instant start = Instant.now();

    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();

    /**
     * This mimics Clojure's notion of keeping mutable references to immutable values.
     * Mutations are guarded by atomic semantics.
     *
     * @see AtomicReference#updateAndGet(UnaryOperator)
     */
    private final AtomicReference<ImList<T>> ref = new AtomicReference<>(vec());

    private final AtomicReference<State<T, R>> state = new AtomicReference<>(new Waiting());

    private final Spec<T, R> spec;
    private final Consumer<Subscription<T>> unregister;

    private interface State<T, R> {

        default State<T, R> deliver(final Deliverable<T> deliverable) {
            return this;
        }

        default State<T, R> cancel() {
            return this;
        }

        default State<T, R> await() throws InterruptedException {
            return this;
        }

        R get(Collection<T> queue) throws TimeoutException;

    }

    private final class Waiting implements State<T, R> {

        @Override
        public State<T, R> deliver(final Deliverable<T> deliverable) {
            final ImList<T> queue = enqueue(deliverable);

            lock.lock();

            try {
                final var received = queue.size();
                if (spec.isDone(received)) {
                    if (spec.isSuccess(received)) {
                        return new Success();
                    } else {
                        return new Failed();
                    }
                }

                return this;
            } finally {
                lock.unlock();
            }
        }

        private ImList<T> enqueue(final Deliverable<T> deliverable) {
            final var message = deliverable.getMessage();
            return ref.updateAndGet(queue -> queue.append(message));
        }

        @Override
        public State<T, R> cancel() {
            return new Cancelled();
        }

        @Override
        public State<T, R> await() throws InterruptedException {
            final var deadline = start.plus(spec.getTimeout());
            // TODO calculate deadline upfront?!
            final var timeout = Duration.between(Instant.now(), deadline).toNanos();

            final var timedOut = await(timeout);
            final var success = spec.isSuccess(ref.get().size());

            if (timedOut) {
                if (success) {
                    return new Success();
                }
                return new TimedOut();
            }

            if (success) {
                return new Success();
            } else {
                return new Failed();
            }
        }

        private boolean await(final long timeout) throws InterruptedException {
            lock.lock();
            try {
                return  !done.await(timeout, NANOSECONDS);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public R get(final Collection<T> queue) {
            throw new UnsupportedOperationException();
        }

    }

    private final class TimedOut implements State<T, R> {

        TimedOut() {
            unregister();
        }

        @Override
        public R get(final Collection<T> queue) throws TimeoutException {
            throw new TimeoutException(spec.format(queue.size()));
        }

    }

    private final class Failed implements State<T, R> {

        Failed() {
            unregister();
        }

        @Override
        public R get(final Collection<T> queue) {
            throw new IllegalStateException(spec.format(queue.size()));
        }
    }

    private final class Success implements State<T, R> {

        Success() {
            unregister();
        }

        @Override
        public R get(final Collection<T> queue) {
            return spec.collect(unmodifiableCollection(queue));
        }

    }

    private final class Cancelled implements State<T, R> {

        Cancelled() {
            unregister();
        }

        @Override
        public R get(final Collection<T> queue) {
            throw new CancellationException();
        }

    }

    @Override
    public Class<T> getMessageType() {
        return spec.getMessageType();
    }

    @Override
    public boolean test(final T input) {
        return spec.test(input);
    }

    @Override
    public boolean deliver(final Deliverable<T> deliverable) {
        final var next = state.updateAndGet(current -> current.deliver(deliverable));
        return !(next instanceof DefaultSubscription.Waiting);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return state.updateAndGet(State::cancel) instanceof DefaultSubscription.Cancelled;
    }

    @Override
    public boolean isCancelled() {
        return state.get() instanceof DefaultSubscription.Cancelled;
    }

    @Override
    public boolean isDone() {
        // TODO what if it timed out just now?!
        return !(state.get() instanceof DefaultSubscription.Waiting);
    }

    @Override
    public R get() throws ExecutionException {
        try {
            final var awaited = state.updateAndGet(throwingUnaryOperator(State::await));
            final var queue = ref.get();
            return awaited.get(queue);
        } catch (final CancellationException e) {
            throw e;
        } catch (final TimeoutException | RuntimeException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public R get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    private void unregister() {
        unregister.accept(this);
    }

}
