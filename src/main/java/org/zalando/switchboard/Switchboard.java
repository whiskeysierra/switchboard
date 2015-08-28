package org.zalando.switchboard;

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

import java.util.List;
import java.util.concurrent.Future;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received messages
 */
public interface Switchboard {

    <T, R, X extends Exception> R receive(final Subscription<T, ?> subscription, final SubscriptionMode<T, R, X> mode, final Timeout timeout)
            throws X, InterruptedException;

    <T, R, X extends Exception> Future<R> subscribe(final Subscription<T, ?> subscription, final SubscriptionMode<T, R, X> mode);

    <T, H> List<H> inspect(Class<T> messageType, Class<H> hintType);

    <T> void send(final Deliverable<T> deliverable);

    static Switchboard create() {
        return new DefaultSwitchboard();
    }

}
