package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.contracts.DeliveryContract;
import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.traits.BroadcastDeliveryTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

public final class BroadcastDeliveryTest implements BroadcastDeliveryTrait, DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    public void shouldDeliverFirstMessageToAllSubscriptions() throws ExecutionException, InterruptedException {
        final Future<Message> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<Message> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        final Message first = firstResult.get();
        final Message second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(first, is(sameInstance(second)));
    }

}
