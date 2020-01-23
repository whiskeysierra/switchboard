package switchboard;

import lombok.AllArgsConstructor;
import org.organicdesign.fp.collections.ImList;

import java.util.Collection;
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
    private final Condition done = lock.newCondition();
    private final Locking locking = Locking.of(lock);

    private final SubscriptionMode<T, R> mode;
    private final Timeout timeout;
    private final Runnable unregister;

    @Override
    public State<T, R> deliver(final Deliverable<T, ?> deliverable) {
        return locking.run(() -> {
            final var message = deliverable.getMessage();
            final Collection<T> queue = ref.updateAndGet(
                    current -> current.append(message));
            final var received = queue.size();

            if (mode.isDone(received)) {
                unregister();
                done.signalAll();

                if (mode.isSuccess(received)) {
                    return success(queue);
                }
                return failed(received);
            }

            return this;
        });
    }

    @Override
    public State<T, R> cancel() {
        return new Cancelled<>();
    }

    @Override
    public State<T, R> await() throws InterruptedException {
        return await(timeout.remaining().toNanos(), NANOSECONDS);
    }

    @Override
    public State<T, R> await(final long timeout, final TimeUnit timeoutUnit)
            throws InterruptedException {
        return locking.run(() -> {
            done.await(timeout, timeoutUnit);

            final var timedOut = this.timeout.isTimedOut();

            final var queue = ref.get();
            final var received = queue.size();

            final var isDoneEarly = mode.isDone(received);

            if (isDoneEarly || timedOut) {
                unregister();

                final var success = mode.isSuccess(received);

                if (success) {
                    return success(queue);
                }

                if (timedOut) {
                    return timedOut(received);
                }

                return failed(received);
            } else {
                return this;
            }
        });
    }

    private Success<T, R> success(final Collection<T> queue) {
        return new Success<>(mode, queue);
    }

    private TimedOut<T, R> timedOut(final int received) {
        return new TimedOut<>(timeout.format(mode, received));
    }

    private Failed<T, R> failed(final int received) {
        return new Failed<>(timeout.format(mode, received));
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
