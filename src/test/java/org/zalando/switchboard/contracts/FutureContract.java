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
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.Future;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

public interface FutureContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void successfulFutureShouldBeDone() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), atLeastOnce());
        unit.send(message(eventA(), deliveryMode()));

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void successfulFutureShouldNotBeCancelled() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), atLeastOnce());
        unit.send(message(eventA(), deliveryMode()));

        assertThat(future.isCancelled(), is(false));
    }

    @Test
    default void cancelledFutureShouldBeDone() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        assertThat(future.isDone(), is(true));
    }

    @Test
    default void cancelledFutureShouldBeCancelled() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());

        future.cancel(false);

        assertThat(future.isCancelled(), is(true));
    }

    @Test
    default void cancellingWaitingFutureShouldSucceed() {
        final Switchboard unit = Switchboard.create();

        assertThat(unit.subscribe(matchA(), exactlyOnce()).cancel(false), is(true));
    }

    @Test
    default void cancellingDoneFutureShouldNotSucceed() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), atLeastOnce());
        unit.send(message(eventA(), deliveryMode()));

        assertThat(future.cancel(true), is(false));
    }

    @Test
    default void cancellingCancelledFutureShouldSucceed() {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        assertThat(future.cancel(false), is(true));
    }

}
