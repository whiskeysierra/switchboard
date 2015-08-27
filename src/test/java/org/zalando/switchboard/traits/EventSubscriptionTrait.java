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

import org.zalando.switchboard.model.Event;
import org.zalando.switchboard.model.EventSubscription;
import org.zalando.switchboard.Subscription;

public interface EventSubscriptionTrait extends SubscriptionTrait<Event> {

    @Override
    default Subscription<Event, ?> matchA() {
        return new EventSubscription("A");
    }

    @Override
    default Subscription<Event, ?> matchB() {
        return new EventSubscription("B");
    }

    @Override
    default Event eventA() {
        return new Event("A");
    }

}
