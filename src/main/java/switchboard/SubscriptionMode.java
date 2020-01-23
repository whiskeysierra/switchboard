package switchboard;

import java.util.Collection;
import java.util.Optional;

public interface SubscriptionMode<T, R> {

    /**
     *
     * @param received the amount of received messages, greater than or equal to 1
     * @return true if this mode can determine it's success condition already
     */
    boolean isDone(int received);

    /**
     *
     * @param received the amount of received messages, greater than or equal to 0
     * @return true if this mode considers the amount of messages successful
     */
    boolean isSuccess(int received);

    R transform(Collection<T> results);

    /**
     * Returns a mode that succeeds if no message was received within the
     * specified timeout.
     *
     * @param <T> message type
     * @return a new subscription mode
     */
    static <T> SubscriptionMode<T, Void> never() {
        return new Never<>();
    }

    /**
     * Returns a mode that succeeds if the number of messages received within
     * the specified timeout is less than or equal to the given count.
     *
     * @param count the maximum number of expected messages
     * @param <T> message type
     * @return a new subscription mode
     */
    static <T> SubscriptionMode<T, Collection<T>> atMost(final int count) {
        return new AtMost<>(count);
    }

    /**
     * Returns a mode that succeeds if zero or one messages are received within
     * the specified timeout.
     *
     * @param <T> message type
     * @return a new subscription mode
     */
    static <T> SubscriptionMode<T, Optional<T>> atMostOnce() {
        return new AtMostOnce<>();
    }

    /**
     * Returns a mode that succeeds if exactly one message is received within
     * the specified timeout.
     *
     * @param <T> message type
     * @return a new subscription mode
     */
    static <T> SubscriptionMode<T, T> exactlyOnce() {
        return new ExactlyOnce<>();
    }

    /**
     * Returns a mode that succeeds if the number of messages received within
     * the specified timeout is exactly the given count.
     *
     * @param count the exact number of expected messages
     * @param <T> message type
     * @return a new subscription mode
     */
    static <T> SubscriptionMode<T, Collection<T>> times(final int count) {
        return new Times<>(count);
    }

    /**
     * Returns a mode that succeeds if at least one message is received within
     * the specified timeout.
     *
     * @param <T> message type
     * @return a new subscription mode
     */
    static <T> SubscriptionMode<T, T> atLeastOnce() {
        return new AtLeastOnce<>();
    }

    /**
     * Returns a mode that succeeds if the number of messages received within
     * the specified timeout is at least the given count.
     *
     * @param count the minimum number of expected messages
     * @param <T> message type
     * @return a new subscription mode
     */
    static <T> SubscriptionMode<T, Collection<T>> atLeast(final int count) {
        return new AtLeast<>(count);
    }

}
