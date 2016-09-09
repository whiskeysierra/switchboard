package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.contracts.DeliveryContract;
import org.zalando.switchboard.traits.DirectDeliveryTrait;

import static org.junit.jupiter.api.Assertions.expectThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

public final class DirectDeliveryTest implements DirectDeliveryTrait, DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    public void shouldThrowWhenDeliveringMessagesToSubscriptions() {
        unit.subscribe(matchA(), exactlyOnce());
        unit.subscribe(matchA(), exactlyOnce());

        expectThrows(IllegalStateException.class, () -> {
            unit.send(message(messageA(), deliveryMode()));
        });
    }

}
