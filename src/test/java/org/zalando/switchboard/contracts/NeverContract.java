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
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.Timeout.within;

public interface NeverContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldNotFailIfExpectedNoneAndReceivedNone() throws InterruptedException, TimeoutException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        unit.receive("foo"::equals, never(), within(1, NANOS));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithTimeout() {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        final IllegalStateException exception = expectThrows(IllegalStateException.class, () -> {
            unit.receive("foo"::equals, never(), within(1, NANOS));
        });

        assertThat(exception.getMessage(), is("Expected no Object message(s), but got 1 in 1 nanoseconds"));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOneWithoutTimeout() {
        final Switchboard unit = Switchboard.create();

        unit.send(message("foo", deliveryMode()));

        final IllegalStateException exception = expectThrows(IllegalStateException.class, () -> {
            unit.subscribe("foo"::equals, never()).get();
        });

        assertThat(exception.getMessage(), is("Expected no Object message(s), but got 1"));
    }

}
