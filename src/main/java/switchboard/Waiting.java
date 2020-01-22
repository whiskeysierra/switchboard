package switchboard;

import lombok.AllArgsConstructor;
import org.organicdesign.fp.collections.ImList;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.organicdesign.fp.StaticImports.vec;

@AllArgsConstructor
final class Waiting<T, R> implements State<T, R> {

    /**
     * This mimics Clojure's notion of keeping mutable references to immutable values.
     * Mutations are guarded by atomic semantics.
     *
     * @see AtomicReference#updateAndGet(UnaryOperator)
     */
    private final AtomicReference<ImList<T>> ref = new AtomicReference<>(vec());

    private final Lock lock = new ReentrantLock();
    private final Condition doneEarly = lock.newCondition();

    private final Spec<T, R> spec;
    private final Runnable unregister;

    @Override
    public State<T, R> deliver(final Deliverable<T, ?> deliverable) {
        lock.lock();

        try {
            final var message = deliverable.getMessage();
            final ImList<T> queue = ref.updateAndGet(current -> current.append(message));
            final var received = queue.size();

            if (spec.isDoneEarly(received)) {
                unregister();
                doneEarly.signalAll();

                if (spec.isSuccess(received)) {
                    return new Success<>(spec, queue);
                }
                return new Failed<>(spec, received);
            }

            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public State<T, R> cancel() {
        return new Cancelled<>();
    }

    @Override
    public State<T, R> await() throws InterruptedException {
        // TODO this branch should be able to rely on the return value of doneEarly.await
        // TODO this method shouldn't technically be possible to return the Waiting state
        return await(spec.getRemainingTimeout(), NANOSECONDS);
    }

    @Override
    public State<T, R> await(final long timeout, final TimeUnit timeoutUnit) throws InterruptedException {
        lock.lock();

        try {
            doneEarly.await(timeout, timeoutUnit);

            // TODO is there a race condition in here?!

            final var timedOut = spec.isTimedOut();

            final var queue = ref.get();
            final var received = queue.size();

            final var isDoneEarly = spec.isDoneEarly(received);

            if (isDoneEarly || timedOut) {
                unregister();

                final var success = spec.isSuccess(received);

                if (success) {
                    return new Success<>(spec, queue);
                }

                if (timedOut) {
                    return new TimedOut<>(spec, received);
                }

                return new Failed<>(spec, received);
            } else {
                return this;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public R get() throws TimeoutException {
        throw new TimeoutException();
    }

    private void unregister() {
        unregister.run();
    }

}
