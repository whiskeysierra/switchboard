package org.zalando.switchboard;

import java.util.List;

public class Times<S> implements SubscriptionMode<S, List<S>> {

    private final int count;

    Times(final int count) {
        this.count = count;
    }

    @Override
    public boolean isDone(final int received) {
        return received > count;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received == count;
    }

    @Override
    public List<S> collect(final List<S> results) {
        return results;
    }

    @Override
    public String toString() {
        return "exactly " + count;
    }

}
