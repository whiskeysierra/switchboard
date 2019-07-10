package org.zalando.switchboard;

import java.util.List;

final class ExactlyOnce<S> implements SubscriptionMode<S, S> {

    @Override
    public boolean isDone(final int received) {
        return received > 1;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received == 1;
    }

    @Override
    public S collect(final List<S> results) {
        return results.get(0);
    }

    @Override
    public String toString() {
        return "exactly one";
    }

}
