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

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.Timeout.in;

public interface NeverContract<S> extends SubscriptionTrait<S>, DeliveryTrait, ExpectedExceptionTrait {

    @Test
    default void shouldNotFailIfExpectedNoneAndReceivedNone() throws InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.receive("foo"::equals, never(), in(1, NANOSECONDS));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithTimeout() throws InterruptedException {
        exception().expect(IllegalStateException.class);
        exception().expectMessage("Expected no Object message(s), but got 1 in 1 nanoseconds");

        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        unit.receive("foo"::equals, never(), in(1, NANOSECONDS));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithoutTimeout() throws ExecutionException, InterruptedException {
        exception().expect(IllegalStateException.class);
        exception().expectMessage(equalTo("Expected no Object message(s), but got 1"));

        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        unit.subscribe("foo"::equals, never()).get();
    }

}
