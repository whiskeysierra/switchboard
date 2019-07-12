package org.zalando.switchboard;

import java.util.Collection;

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
    public S collect(final Collection<S> results) {
        return results.iterator().next();
    }

    @Override
    public String toString() {
        return "at least one";
    }
}
