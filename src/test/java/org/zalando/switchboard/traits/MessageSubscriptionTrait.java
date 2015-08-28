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

import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.model.MessageSubscription;
import org.zalando.switchboard.Subscription;

public interface MessageSubscriptionTrait extends SubscriptionTrait<Message> {

    @Override
    default Subscription<Message, ?> matchA() {
        return new MessageSubscription("A");
    }

    @Override
    default Subscription<Message, ?> matchB() {
        return new MessageSubscription("B");
    }

    @Override
    default Message messageA() {
        return new Message("A");
    }

}
