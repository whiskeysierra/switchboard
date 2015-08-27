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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.zalando.switchboard.SubscriptionMode.times;
import static org.zalando.switchboard.Timeout.in;

public interface TimeoutTestTrait<S> extends SubscriptionTrait<S>, DeliveryTrait, ExpectedExceptionTrait {

    @Test(timeout = 250)
    default void shouldTellThatThirdEventDidNotOccur() throws TimeoutException {
        final Switchboard unit = Switchboard.create();

        unit.send(eventA(), deliveryMode());
        unit.send(eventA(), deliveryMode());

        exception().expect(TimeoutException.class);
        exception().expectMessage(containsString("3rd"));

        unit.receive(matchA(), times(3), in(100, MILLISECONDS));
    }

    @Test(timeout = 250)
    default void shouldTellThatThirdEventDidNotOccurWhenPollingAsync() throws TimeoutException, ExecutionException, InterruptedException {
        final Switchboard unit = Switchboard.create();

        unit.send(eventA(), deliveryMode());
        unit.send(eventA(), deliveryMode());

        exception().expect(TimeoutException.class);
        exception().expectMessage(containsString("3rd"));

        unit.subscribe(matchA(), times(3)).get(100, MILLISECONDS);
    }

}
