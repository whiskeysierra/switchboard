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
import org.zalando.switchboard.DeliveryMode;
import org.zalando.switchboard.Switchboard;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.in;

public interface UnsubscribeTestTrait<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldUnsubscribe() throws TimeoutException {
        final Switchboard unit = Switchboard.create();

        // expected to unsubscribe itself in 1 ns
        unit.receive("foo"::equals, never(), in(1, TimeUnit.NANOSECONDS));

        unit.send("foo", DeliveryMode.DIRECT);
        final String actual = unit.receive("foo"::equals, exactlyOnce(), in(1, TimeUnit.SECONDS));
        assertThat(actual, is("foo"));
    }

    @Test(expected = TimeoutException.class)
    default void cancellingFutureShouldUnsubscribe() throws InterruptedException, ExecutionException, TimeoutException {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        unit.send(eventA(), deliveryMode());
        future.get(100, MILLISECONDS);
    }

}
