package switchboard;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

interface State<T, R> {

    default State<T, R> deliver(final Deliverable<T, ?> deliverable) {
        return this;
    }

    default State<T, R> cancel() {
        return this;
    }

    default State<T, R> await() throws InterruptedException {
        return this;
    }

    default State<T, R> await(final long timeout, final TimeUnit timeoutUnit) throws InterruptedException {
        return this;
    }

    default boolean isDone() {
        return true;
    }

    default boolean isCancelled() {
        return false;
    }

    R get() throws TimeoutException;

}
