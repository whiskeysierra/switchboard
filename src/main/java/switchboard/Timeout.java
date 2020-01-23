package switchboard;

import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.Instant;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor(access = PRIVATE)
final class Timeout {

    private final Duration timeout;
    private final Instant deadline;

    Timeout(final Duration timeout) {
        this(timeout, now().plus(timeout));
    }

    Duration remaining() {
        return between(now(), deadline);
    }

    boolean isTimedOut() {
        return now().isAfter(deadline);
    }

    String format(final SubscriptionMode<?, ?> mode, final int received) {
        return String.format(
                "Expected to receive %s message(s) within %s, but got %d",
                mode, timeout, received);
    }

}
