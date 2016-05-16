package org.zalando.switchboard.contracts;

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
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

public interface RecordContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldDeliverRecordedMessagesToSubscriptions() throws ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverRecordedMessagesToConcurrentSubscriptions() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        final S first = firstResult.get();
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverRecordedMessagesToSubscriptionsOneAtATime() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverPartlyRecordedMessagesToSubscriptionsOneAtATime() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final S first = firstResult.get();

        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(message(messageA(), deliveryMode()));

        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldDeliverPartlyRecordedMessagesToConcurrentSubscriptions() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));

        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());
        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(message(messageA(), deliveryMode()));

        final S first = firstResult.get();
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

}
