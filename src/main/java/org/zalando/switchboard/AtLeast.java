package org.zalando.switchboard;

import java.util.Collection;

final class AtLeast<S> implements SubscriptionMode<S, Collection<S>> {

    private final int count;

    AtLeast(final int count) {
        this.count = count;
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
    public Collection<S> collect(final Collection<S> results) {
        return results;
    }

    @Override
    public String toString() {
        return "at least " + count;
    }
}
