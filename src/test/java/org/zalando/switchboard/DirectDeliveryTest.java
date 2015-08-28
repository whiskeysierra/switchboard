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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.zalando.switchboard.contracts.AtLeastContract;
import org.zalando.switchboard.contracts.AtLeastOnceContract;
import org.zalando.switchboard.contracts.AtMostContract;
import org.zalando.switchboard.contracts.ExactlyOnceContract;
import org.zalando.switchboard.contracts.FailContract;
import org.zalando.switchboard.contracts.FutureContract;
import org.zalando.switchboard.contracts.InspectContract;
import org.zalando.switchboard.contracts.NeverContract;
import org.zalando.switchboard.contracts.RecordContract;
import org.zalando.switchboard.contracts.SubscribeContract;
import org.zalando.switchboard.contracts.TimeoutContract;
import org.zalando.switchboard.contracts.TimesContract;
import org.zalando.switchboard.contracts.UnsubscribeContract;
import org.zalando.switchboard.framework.Java8JunitClassRunner;
import org.zalando.switchboard.model.Event;
import org.zalando.switchboard.traits.DirectDeliveryTrait;
import org.zalando.switchboard.traits.EventSubscriptionTrait;

import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

@RunWith(Java8JunitClassRunner.class)
public final class DirectDeliveryTest implements DirectDeliveryTrait, EventSubscriptionTrait,
        AtLeastContract<Event>,
        AtLeastOnceContract<Event>,
        AtMostContract<Event>,
        ExactlyOnceContract<Event>,
        FailContract<Event>,
        FutureContract<Event>,
        InspectContract<Event>,
        NeverContract<Event>,
        RecordContract<Event>,
        SubscribeContract<Event>,
        TimeoutContract<Event>,
        TimesContract<Event>,
        UnsubscribeContract<Event> {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final Switchboard unit = Switchboard.create();

    @Override
    public ExpectedException exception() {
        return exception;
    }

    @Test(expected = IllegalStateException.class, timeout = 250)
    public void shouldThrowWhenDeliveringEventsToSubscriptions() {
        unit.subscribe(matchA(), exactlyOnce());
        unit.subscribe(matchA(), exactlyOnce());

        unit.send(message(eventA(), DeliveryMode.DIRECT));
        unit.send(message(eventA(), DeliveryMode.DIRECT));
    }

}
