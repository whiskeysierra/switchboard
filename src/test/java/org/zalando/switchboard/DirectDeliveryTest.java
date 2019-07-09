package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.zalando.switchboard.contracts.DeliveryContract;
import org.zalando.switchboard.traits.DirectDeliveryTrait;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

final class DirectDeliveryTest implements DirectDeliveryTrait, DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test
    void shouldThrowWhenDeliveringMessagesToSubscriptions() {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            unit.subscribe(matchA(), exactlyOnce());
            unit.subscribe(matchA(), exactlyOnce());

            assertThrows(IllegalStateException.class, () ->
                    unit.send(message(messageA(), deliveryMode())));
        });
    }

}
