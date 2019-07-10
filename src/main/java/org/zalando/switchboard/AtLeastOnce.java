package org.zalando.switchboard;

import java.util.List;

final class AtLeastOnce<S> implements SubscriptionMode<S, S> {

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
