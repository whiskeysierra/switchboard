package switchboard;

import java.util.Collection;
import java.util.Optional;

public interface SubscriptionMode<T, R> {

    // TODO find better name!

    /**
     *
     * @param received the amount of received messages, greater than or equal to 1
     * @return true if this mode can determine it's success condition already
     */
    boolean isDoneEarly(int received);

    /**
     *
     * @param received the amount of received messages, greater than or equal to 0
     * @return true if this mode considers the amount of messages successful
     */
    boolean isSuccess(int received);

    R transform(Collection<T> results);

    // TODO document: non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, Void> never() {
        return new Never<>();
    }

    // TODO document: non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, Collection<S>> atMost(final int count) {
        return new AtMost<>(count);
    }

    static <S> SubscriptionMode<S, Optional<S>> atMostOnce() {
        return new AtMostOnce<>();
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
