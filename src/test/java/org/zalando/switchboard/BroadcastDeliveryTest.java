package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.zalando.switchboard.contracts.DeliveryContract;
import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.traits.BroadcastDeliveryTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

@RunWith(JUnitPlatform.class)
public final class BroadcastDeliveryTest implements BroadcastDeliveryTrait, DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test
    public void shouldDeliverFirstMessageToAllSubscriptions() throws ExecutionException, InterruptedException {
        assertTimeout(TestTimeout.DEFAULT, () -> {
            final Future<Message> firstResult = unit.subscribe(matchA(), atLeastOnce());
            final Future<Message> secondResult = unit.subscribe(matchA(), atLeastOnce());

            unit.send(message(messageA(), deliveryMode()));
            unit.send(message(messageA(), deliveryMode()));

            final Message first = firstResult.get();
            final Message second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(first, is(sameInstance(second)));
        });
    }

}
