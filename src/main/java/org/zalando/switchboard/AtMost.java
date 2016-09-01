package org.zalando.switchboard;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class AtMost<S> implements SubscriptionMode<S, List<S>> {

    private final int count;

    public AtMost(final int count) {
        this.count = count;
    }

    @Override
    public List<S> block(final Future<List<S>> future, final long timeout, final TimeUnit timeoutUnit)
            throws ExecutionException, InterruptedException, TimeoutException {
        return future.get(timeout, timeoutUnit);
    }

    @Override
    public boolean isDone(final int received) {
        return received > count;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received <= count;
    }

    @Override
    public List<S> collect(final List<S> results) {
        return results;
    }

    @Override
    public String toString() {
        return "at most " + count;
    }

}
