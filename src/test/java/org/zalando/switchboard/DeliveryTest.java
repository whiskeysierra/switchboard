package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.contracts.DeliveryContract;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

final class DeliveryTest implements DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test
    void shouldDeliverFirstMessageToAllSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            unit.publish(message(messageA()));
            unit.publish(message(messageA()));

            final var first = firstResult.get(1, NANOSECONDS);
            final var second = secondResult.get(1, NANOSECONDS);

            assertThat(first, is(messageA()));
            assertThat(first, is(sameInstance(second)));
        });
    }

}
