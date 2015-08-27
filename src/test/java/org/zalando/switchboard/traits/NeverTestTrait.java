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

import java.util.concurrent.TimeUnit;

import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.Timeout.in;

public interface NeverTestTrait<S> extends SubscriptionTrait<S>, DeliveryTrait, ExpectedExceptionTrait {
    
    @Test
    default void shouldNotFailIfExpectedNoneAndReceivedNone() {
        final Switchboard unit = Switchboard.create();

        unit.receive("foo"::equals, never(), in(100, TimeUnit.MILLISECONDS));
    }

    @Test
    default void shouldFailIfExpectedNoneButReceivedOne() {
        exception().expect(IllegalStateException.class);
        // TODO expect message

        final Switchboard unit = Switchboard.create();

        unit.send("foo", DeliveryMode.DIRECT);

        unit.receive("foo"::equals, never(), in(100, TimeUnit.MILLISECONDS));
    }

}
