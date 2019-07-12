package org.zalando.switchboard;

import java.util.Collection;

final class Never<S> implements SubscriptionMode<S, Void> {

    @Override
    public boolean isDone(final int received) {
        return received > 0;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received == 0;
    }

    @Override
    public Void collect(final Collection<S> results) {
        return null;
    }

    @Override
    public String toString() {
        return "not even one";
    }

}
