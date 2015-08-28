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
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.times;
import static org.zalando.switchboard.Timeout.in;

public interface TimeoutContract<S> extends SubscriptionTrait<S>, DeliveryTrait, ExpectedExceptionTrait {

    @Test(timeout = 250)
    default void shouldTellThatThirdMessageDidNotOccur() throws TimeoutException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        exception().expect(TimeoutException.class);
        exception().expectMessage(containsString("Expected exactly 3 Message message(s), but got 2 in 1 nanoseconds"));

        unit.receive(matchA(), times(3), in(1, NANOSECONDS));
    }

    @Test(timeout = 250)
    default void shouldTellThatThirdMessageDidNotOccurWhenPollingAsync() throws TimeoutException, ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send(message(messageA(), deliveryMode()));
        unit.send(message(messageA(), deliveryMode()));

        exception().expect(TimeoutException.class);
        exception().expectMessage(containsString("Expected exactly 3 Message message(s), but got 2 in 1 nanoseconds"));

        unit.subscribe(matchA(), times(3)).get(1, NANOSECONDS);
    }

}
