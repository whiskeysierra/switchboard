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
import org.zalando.switchboard.traits.BroadcastDeliveryTrait;
import org.zalando.switchboard.traits.EventSubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

@RunWith(Java8JunitClassRunner.class)
public final class BroadcastDeliveryTest implements BroadcastDeliveryTrait, EventSubscriptionTrait,
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

    @Test(timeout = 250)
    public void shouldDeliverFirstEventToAllSubscriptions() throws ExecutionException, InterruptedException {
        final Future<Event> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<Event> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(eventA(), DeliveryMode.BROADCAST);
        unit.send(eventA(), DeliveryMode.BROADCAST);

        final Event first = firstResult.get();
        final Event second = secondResult.get();

        assertThat(first, is(eventA()));
        assertThat(first, is(sameInstance(second)));
    }

}
