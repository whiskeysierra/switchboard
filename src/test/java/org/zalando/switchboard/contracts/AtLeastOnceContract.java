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
import org.zalando.switchboard.traits.ExpectedExceptionTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.Timeout.in;

public interface AtLeastOnceContract<S> extends SubscriptionTrait<S>, DeliveryTrait, ExpectedExceptionTrait {

    @Test
    default void shouldFailIfExpectedAtLeastOneButReceivedNone() throws TimeoutException, InterruptedException {
        exception().expect(TimeoutException.class);
        exception().expectMessage("Expected at least one Object event(s), but got 0 in 1 nanoseconds");

        final Switchboard unit = Switchboard.create();

        unit.receive("foo"::equals, atLeastOnce(), in(1, NANOSECONDS));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedExactlyOne() throws TimeoutException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send("foo", deliveryMode());

        unit.receive("foo"::equals, atLeastOnce(), in(1, NANOSECONDS));
    }

    @Test
    default void shouldNotFailIfExpectedAtLeastOneAndReceivedTwo() throws TimeoutException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send("foo", deliveryMode());
        unit.send("foo", deliveryMode());

        unit.receive("foo"::equals, atLeastOnce(), in(1, NANOSECONDS));
    }

}
