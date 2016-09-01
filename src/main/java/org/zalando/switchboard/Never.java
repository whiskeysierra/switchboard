package org.zalando.switchboard;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class Never<S> implements SubscriptionMode<S, Void> {

    @Override
    public Void block(final Future<Void> future, final long timeout, final TimeUnit timeoutUnit)
            throws InterruptedException, ExecutionException {
        try {
            return future.get(timeout, timeoutUnit);
        } catch (final TimeoutException ignored) {
            return null;
        }
    }

    @Override
    public boolean isDone(final int received) {
        return received > 0;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received == 0;
    }

    @Override
    public Void collect(final List<S> results) {
        return null;
    }

    @Override
    public String toString() {
        return "no";
    }

}
