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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.Timeout.within;

public interface UnsubscribeContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldUnsubscribe() throws TimeoutException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        // expected to unsubscribe itself in 1 ns
        unit.receive("foo"::equals, never(), within(1, NANOS));

        unit.send(message("foo", deliveryMode()));
        final String actual = unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        assertThat(actual, is("foo"));
    }

    @Test(expected = TimeoutException.class)
    default void cancellingFutureShouldUnsubscribe() throws InterruptedException, ExecutionException, TimeoutException {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        unit.send(message(messageA(), deliveryMode()));
        future.get(1, NANOSECONDS);
    }

}
