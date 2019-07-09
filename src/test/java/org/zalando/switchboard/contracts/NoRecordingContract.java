package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.NoAnsweringMachine;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.Timeout.within;

public interface NoRecordingContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldNotRecordMessages() {
        final var unit = Switchboard.create(new NoAnsweringMachine());

        unit.send(message(messageA(), deliveryMode()));

        assertThrows(TimeoutException.class, () ->
            unit.receive(matchA(), atLeastOnce(), within(1, NANOS)));
    }

}
