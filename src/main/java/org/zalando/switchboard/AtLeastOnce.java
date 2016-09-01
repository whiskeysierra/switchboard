package org.zalando.switchboard;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class AtLeastOnce<S> implements SubscriptionMode<S, S> {

    @Override
    public S block(final Future<S> future, final long timeout, final TimeUnit timeoutUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, timeoutUnit);
    }

    @Override
    public boolean isDone(final int received) {
        return received > 0;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received > 0;
    }

    @Override
    public S collect(final List<S> results) {
        return results.get(0);
    }

    @Override
    public String toString() {
        return "at least one";
    }
}
