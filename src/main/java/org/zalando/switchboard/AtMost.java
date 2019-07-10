package org.zalando.switchboard;

import java.util.List;

final class AtMost<S> implements SubscriptionMode<S, List<S>> {

    private final int count;

    AtMost(final int count) {
        this.count = count;
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
