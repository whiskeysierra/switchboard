package org.zalando.switchboard.contracts;

/*
 * ⁣​
 * Switchboard
 * ⁣⁣
 * Copyright (C) 2015 - 2016 Zalando SE
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
import org.zalando.switchboard.traits.MessageSubscriptionTrait;

public interface DeliveryContract extends MessageSubscriptionTrait,
        AtLeastContract<Message>,
        AtLeastOnceContract<Message>,
        AtMostContract<Message>,
        ExactlyOnceContract<Message>,
        FailContract<Message>,
        FutureContract<Message>,
        InspectContract<Message>,
        NeverContract<Message>,
        RecordContract<Message>,
        SubscribeContract<Message>,
        TimeoutContract<Message>,
        TimesContract<Message>,
        UnsubscribeContract<Message> {
}
