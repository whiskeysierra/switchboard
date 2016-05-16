package org.zalando.switchboard;

/*
 * ⁣​
 * Switchboard
 * ⁣⁣
 * Copyright (C) 2015 Zalando SE
 * ⁣⁣
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ​⁣
 */

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
