package org.zalando.switchboard.traits;

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

import org.junit.Test;
import org.zalando.switchboard.Switchboard;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

public interface RecordTestTrait<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldDeliverRecordedEventsToSubscriptions() throws ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send(eventA(), deliveryMode());
        unit.send(eventA(), deliveryMode());

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());
        final S second = secondResult.get();

        assertThat(first, is(eventA()));
        assertThat(second, is(eventA()));
    }

    @Test(timeout = 250)
    default void shouldDeliverRecordedEventsToConcurrentSubscriptions() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();
        
        unit.send(eventA(), deliveryMode());
        unit.send(eventA(), deliveryMode());

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        final S first = firstResult.get();
        final S second = secondResult.get();

        assertThat(first, is(eventA()));
        assertThat(second, is(eventA()));
    }

    @Test(timeout = 250)
    default void shouldDeliverRecordedEventsToSubscriptionsOneAtATime() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();
        
        unit.send(eventA(), deliveryMode());

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        unit.send(eventA(), deliveryMode());

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());
        final S second = secondResult.get();

        assertThat(first, is(eventA()));
        assertThat(second, is(eventA()));
    }

    @Test(timeout = 250)
    default void shouldDeliverPartlyRecordedEventsToSubscriptionsOneAtATime() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();
        
        unit.send(eventA(), deliveryMode());

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(eventA(), deliveryMode());

        final S second = secondResult.get();

        assertThat(first, is(eventA()));
        assertThat(second, is(eventA()));
    }

    @Test(timeout = 250)
    default void shouldDeliverPartlyRecordedEventsToConcurrentSubscriptions() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();
        
        unit.send(eventA(), deliveryMode());

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(eventA(), deliveryMode());

        final S first = firstResult.get();
        final S second = secondResult.get();

        assertThat(first, is(eventA()));
        assertThat(second, is(eventA()));
    }

}
