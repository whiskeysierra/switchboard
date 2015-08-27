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

import com.google.common.base.Throwables;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received events
 */
public interface Switchboard {

    static Switchboard create() {
        return new DefaultSwitchboard();
    }

    default <S, T, X extends Exception> T receive(final Subscription<S, ?> subscription, final SubscriptionMode<S, T, X> mode, final Timeout timeout) throws X {
        try {
            final Future<T> future = subscribe(subscription, mode);
            return mode.block(future, timeout.getValue(), timeout.getUnit());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            // TODO is throwing an exception here the right thing?
            throw new RuntimeException("Thread has been interrupted while waiting");
        } catch (final ExecutionException e) {
            Throwables.propagateIfPossible(e.getCause());
            throw new IllegalStateException(e.getCause());
        }
    }

    <S, T, X extends Exception> Future<T> subscribe(final Subscription<S, ?> subscription, final SubscriptionMode<S, T, X> mode);

    <E, H> List<H> inspect(Class<E> eventType, Class<H> hintType);

    <E> void send(E event, DeliveryMode deliveryMode);

    <E> void fail(E event, DeliveryMode deliveryMode, RuntimeException exception);

}
