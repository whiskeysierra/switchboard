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
import org.zalando.switchboard.traits.DirectDeliveryTrait;

import static org.junit.gen5.api.Assertions.expectThrows;
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
