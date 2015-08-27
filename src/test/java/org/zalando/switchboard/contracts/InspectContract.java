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
import org.zalando.switchboard.model.Event;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.Subscription.on;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

public interface InspectContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test(timeout = 250)
    default void shouldAllowToInspectPendingEventHints() {
        final Switchboard unit = Switchboard.create();

        unit.subscribe(matchA(), exactlyOnce());
        assertThat(unit.inspect(Event.class, String.class), is(singletonList("A")));

        unit.subscribe(matchA(), exactlyOnce());
        assertThat(unit.inspect(Event.class, String.class), is(asList("A", "A")));

        unit.subscribe(matchB(), exactlyOnce());
        assertThat(unit.inspect(Event.class, String.class), containsInAnyOrder(asList("A", "A", "B").toArray()));
    }

    @Test
    default void shouldProvideHint() {
        final Switchboard unit = Switchboard.create();

        unit.subscribe(on(String.class, "foo"::equals, "bar"), exactlyOnce());

        assertThat(unit.inspect(String.class, String.class), contains("bar"));
    }

}
