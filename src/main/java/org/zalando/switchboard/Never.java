package org.zalando.switchboard;

import java.util.List;
import java.util.concurrent.Future;

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
    public Void collect(final List<S> results) {
        return null;
    }

    @Override
    public String toString() {
        return "not even one";
    }

}
