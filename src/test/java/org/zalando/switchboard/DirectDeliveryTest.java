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
import org.zalando.switchboard.framework.Java8JunitClassRunner;
import org.zalando.switchboard.model.Event;
import org.zalando.switchboard.traits.AtMostTestTrait;
import org.zalando.switchboard.traits.DirectDeliveryTrait;
import org.zalando.switchboard.traits.EventSubscriptionTrait;
import org.zalando.switchboard.traits.FutureTestTrait;
import org.zalando.switchboard.traits.InspectTestTrait;
import org.zalando.switchboard.traits.NeverTestTrait;
import org.zalando.switchboard.traits.ExactlyOnceTestTrait;
import org.zalando.switchboard.traits.RecordTestTrait;
import org.zalando.switchboard.traits.SubscribeTestTrait;
import org.zalando.switchboard.traits.TimeoutTestTrait;
import org.zalando.switchboard.traits.TimesTestTrait;
import org.zalando.switchboard.traits.UnsubscribeTestTrait;

import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

@RunWith(Java8JunitClassRunner.class)
public final class DirectDeliveryTest implements DirectDeliveryTrait, EventSubscriptionTrait,
        AtMostTestTrait<Event>,
        FutureTestTrait<Event>,
        InspectTestTrait<Event>,
        NeverTestTrait<Event>,
        ExactlyOnceTestTrait<Event>,
        RecordTestTrait<Event>,
        SubscribeTestTrait<Event>,
        TimeoutTestTrait<Event>,
        TimesTestTrait<Event>,
        UnsubscribeTestTrait<Event> {

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

        unit.send(eventA(), DeliveryMode.DIRECT);
        unit.send(eventA(), DeliveryMode.DIRECT);
    }

}
