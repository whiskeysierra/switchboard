package org.zalando.switchboard;

import java.util.Collection;

public interface SubscriptionMode<T, R> {

    boolean isDone(int received);
    boolean isSuccess(int received);

    R collect(Collection<T> results);

    // TODO document: non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, Void> never() {
        return new Never<>();
    }

    // TODO document: non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, Collection<S>> atMost(final int count) {
        return new AtMost<>(count);
    }

    // TODO document: exactly, blocking
    static <S> SubscriptionMode<S, S> exactlyOnce() {
        return new ExactlyOnce<>();
    }

    // TODO document: exactly, blocking
    static <S> SubscriptionMode<S, Collection<S>> times(final int count) {
        return new Times<>(count);
    }

    // TODO document: non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, S> atLeastOnce() {
        return new AtLeastOnce<>();
    }

    // TODO document: non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, Collection<S>> atLeast(final int count) {
        return new AtLeast<>(count);
    }

}
