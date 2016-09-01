package org.zalando.switchboard;

import org.junit.gen5.api.Test;
import org.zalando.switchboard.contracts.DeliveryContract;
import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.traits.FirstDeliveryTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

public final class FirstDeliveryTest implements FirstDeliveryTrait, DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    public void shouldDeliverMessagesToFirstSubscriptions() throws ExecutionException, InterruptedException {
        final Future<Message> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<Message> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        final Message first = firstResult.get();
        final Message second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

}
