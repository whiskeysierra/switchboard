package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.zalando.switchboard.contracts.DeliveryContract;
import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.traits.FirstDeliveryTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

final class FirstDeliveryTest implements FirstDeliveryTrait, DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test
    void shouldDeliverMessagesToFirstSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final var firstResult = unit.subscribe(matchA(), atLeastOnce());
            final var secondResult = unit.subscribe(matchA(), atLeastOnce());

            unit.send(message(messageA(), deliveryMode()));
            unit.send(message(messageA(), deliveryMode()));

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(second, is(messageA()));
        });
    }

}
