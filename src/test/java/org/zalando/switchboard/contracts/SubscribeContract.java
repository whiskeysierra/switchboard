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

import org.junit.Test;
import org.zalando.switchboard.Subscription;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.frequency;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeast;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.times;
import static org.zalando.switchboard.Timeout.in;

public interface SubscribeContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test(expected = IllegalStateException.class)
    default void shouldThrowWhenSubscribingTwice() {
        final Switchboard unit = Switchboard.create();

        final Subscription<S, ?> subscription = matchA();
        unit.subscribe(subscription, exactlyOnce());
        unit.subscribe(subscription, exactlyOnce());
    }
    
    @Test(timeout = 250)
    default void shouldDeliverMessageToSubscriptions() throws InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        final Future<S> firstResult = unit.subscribe(matchA(), atLeastOnce());

        unit.send(message(messageA(), deliveryMode()));
        final Future<S> secondResult = unit.subscribe(matchA(), atLeastOnce());

        final S first = firstResult.get();
        final S second = secondResult.get();

        assertThat(first, is(messageA()));
        assertThat(second, is(messageA()));
    }

    @Test(expected = TimeoutException.class, timeout = 250)
    default void shouldTimeoutWhenThereAreNoMatchingMessages() throws TimeoutException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        unit.receive(matchB(), exactlyOnce(), in(1, NANOSECONDS));
    }

    @Test(timeout = 250)
    default void shouldPollMultipleTimesWhenCountGiven() throws TimeoutException, InterruptedException {
        final Switchboard unit = Switchboard.create();
        
        final int count = 5;

        for (int i = 0; i < count; i++) {
            unit.send(message(messageA(), deliveryMode()));
        }

        final List<S> messages = unit.receive(matchA(), times(count), in(1, NANOSECONDS));

        assertThat(messages, hasSize(count));
        assertThat(frequency(messages, messageA()), is(count));
    }

    @Test(timeout = 250)
    default void shouldPollAsyncMultipleTimesWhenCountGiven() throws ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();
        
        final int count = 5;

        final Future<List<S>> future = unit.subscribe(matchA(), atLeast(count));

        for (int i = 0; i < count; i++) {
            unit.send(message(messageA(), deliveryMode()));
        }

        final List<S> messages = future.get();

        assertThat(messages, hasSize(count));
        assertThat(frequency(messages, messageA()), is(count));
    }

    @Test(timeout = 250)
    default void shouldPollAsyncWithTimeoutMultipleTimesWhenCountGiven() throws ExecutionException, InterruptedException, TimeoutException {
        final Switchboard unit = Switchboard.create();
        
        final int count = 5;

        final Future<List<S>> future = unit.subscribe(matchA(), times(count));

        for (int i = 0; i < count; i++) {
            unit.send(message(messageA(), deliveryMode()));
        }

        final List<S> messages = future.get(1, NANOSECONDS);

        assertThat(messages, hasSize(count));
        assertThat(frequency(messages, messageA()), is(count));
    }

}
