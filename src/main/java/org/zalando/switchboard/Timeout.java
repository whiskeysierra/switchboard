package org.zalando.switchboard;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Timeout {

    private Timeout() {

    }

    public static Duration within(final long timeout, final ChronoUnit unit) {
        return Duration.of(timeout, unit);
    }

}
