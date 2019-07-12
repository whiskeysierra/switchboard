package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.Collection;

// TODO find better name!
@AllArgsConstructor
final class Spec<T, R> implements Specification<T>, SubscriptionMode<T, R> {

    private final Specification<T> specification;
    private final SubscriptionMode<T, R> mode;

    @Getter
    private final Duration timeout;

    String format(final int received) {
        // TODO humanize timeout, e.g. 250 milliseconds
        return String.format("Expected to receive %s message(s) within %s, but got %d", mode, timeout, received);
    }

    @Override
    public boolean isDone(final int received) {
        return mode.isDone(received);
    }

    @Override
    public boolean isSuccess(final int received) {
        return mode.isSuccess(received);
    }

    @Override
    public R collect(final Collection<T> results) {
        return mode.collect(results);
    }

    @Override
    public Class<T> getMessageType() {
        return specification.getMessageType();
    }

    @Override
    public boolean test(final T t) {
        return specification.test(t);
    }

}
