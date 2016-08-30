package org.zalando.switchboard;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Timeout {

    Timeout() {
        // package private so we can trick code coverage
    }

    public static Duration within(final long timeout, final ChronoUnit unit) {
        return Duration.of(timeout, unit);
    }

}
