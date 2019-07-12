package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.contracts.DeliveryContract;
import org.zalando.switchboard.model.Message;

import java.time.Duration;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

final class DeliveryTest implements DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test
    void shouldDeliverFirstMessageToAllSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var first = firstResult.join();
            final var second = secondResult.join();

            assertThat(first, is(messageA()));
            assertThat(first, is(sameInstance(second)));
        });
    }

}
