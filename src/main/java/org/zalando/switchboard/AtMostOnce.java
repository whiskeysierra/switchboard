package org.zalando.switchboard;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Iterables.getOnlyElement;

final class AtMostOnce<S> implements SubscriptionMode<S, Optional<S>> {

    @Override
    public boolean isDoneEarly(final int received) {
        return received > 1;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received <= 1;
    }

    @Override
    public Optional<S> transform(final Collection<S> results) {
        return Optional.ofNullable(getOnlyElement(results, null));
    }

    @Override
    public String toString() {
        return "at most one";
    }

}
