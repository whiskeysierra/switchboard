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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.in;

public interface ExactlyOnceTestTrait<S> extends SubscriptionTrait<S>, DeliveryTrait, ExpectedExceptionTrait {

    @Test
    default void shouldFailIfExpectedOneButReceivedNone() throws TimeoutException {
        exception().expect(TimeoutException.class);
        // TODO expect message

        final Switchboard unit = Switchboard.create();

        unit.receive("foo"::equals, exactlyOnce(), in(10, TimeUnit.MILLISECONDS));
    }

    @Test
    default void shouldNotFailIfExpectedOneAndReceivedExactlyOne() throws TimeoutException {
        final Switchboard unit = Switchboard.create();

        unit.send("foo", deliveryMode());

        unit.receive("foo"::equals, exactlyOnce(), in(10, TimeUnit.MILLISECONDS));
    }

    @Test
    default void shouldFailIfExpectedOneButReceivedTwo() throws TimeoutException {
        exception().expect(IllegalStateException.class);
        // TODO expect message

        final Switchboard unit = Switchboard.create();

        unit.send("foo", deliveryMode());
        unit.send("foo", deliveryMode());

        unit.receive("foo"::equals, exactlyOnce(), in(10, TimeUnit.MILLISECONDS));
    }

}
