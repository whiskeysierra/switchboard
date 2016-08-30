package org.zalando.switchboard;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class AtLeast<S> implements SubscriptionMode<S, List<S>> {

    private final int count;

    public AtLeast(final int count) {
        this.count = count;
    }

    @Override
    public List<S> block(final Future<List<S>> future, final long timeout, final TimeUnit timeoutUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, timeoutUnit);
    }

    @Override
    public boolean isDone(final int received) {
        return received >= count;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received >= count;
    }

    @Override
    public List<S> collect(final List<S> results) {
        return results;
    }

    @Override
    public String toString() {
        return "at least " + count;
    }
}
