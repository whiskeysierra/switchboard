package switchboard;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.time.Duration;
import java.time.Instant;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static lombok.AccessLevel.PRIVATE;

// TODO find better name!
@AllArgsConstructor(access = PRIVATE)
final class Spec<T, R> implements SubscriptionMode<T, R> {

    @Delegate
    private final SubscriptionMode<T, R> mode;
    private final Duration timeout;
    private final Instant deadline;

    Spec(final SubscriptionMode<T, R> mode, final Duration timeout) {
        this(mode, timeout, now().plus(timeout));
    }

    long getRemainingTimeout() {
        return between(now(), deadline).toNanos();
    }

    boolean isTimedOut() {
        return now().isAfter(deadline);
    }

    String format(final int received) {
        // TODO humanize timeout, e.g. 250 milliseconds
        return String.format("Expected to receive %s message(s) within %s, but got %d", mode, timeout, received);
    }

}
