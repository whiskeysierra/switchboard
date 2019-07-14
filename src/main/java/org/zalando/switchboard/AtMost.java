package org.zalando.switchboard;

import java.util.Collection;

final class AtMost<S> implements SubscriptionMode<S, Collection<S>> {

    private final int count;

    AtMost(final int count) {
        this.count = count;
    }

    @Override
    public boolean isDoneEarly(final int received) {
        return received > count;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received <= count;
    }

    @Override
    public Collection<S> transform(final Collection<S> results) {
        return results;
    }

    @Override
    public String toString() {
        return "at most " + count;
    }

}
