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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received messages
 */
public interface Switchboard {


    <T, R> R receive(final Subscription<T, ?> subscription, final SubscriptionMode<T, R> mode, final Duration timeout)
            throws ExecutionException, TimeoutException, InterruptedException;

    <T, R> Future<R> subscribe(final Subscription<T, ?> subscription, final SubscriptionMode<T, R> mode);

    <T, H> List<H> inspect(final Class<T> messageType, final Class<H> hintType);

    <T> void send(final Deliverable<T> deliverable);

    static Switchboard create() {
        return new DefaultSwitchboard();
    }

}
