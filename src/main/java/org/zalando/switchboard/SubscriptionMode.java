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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface SubscriptionMode<T, R> {

    default boolean requiresTimeout() {
        return false;
    }

    R block(final Future<R> future, final long timeout, final TimeUnit timeoutUnit) throws ExecutionException, TimeoutException, InterruptedException;

    boolean isDone(int received);

    boolean isSuccess(int received);

    R collect(List<T> results);

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, Void> never() {
        return new Never<>();
    }

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, List<S>> atMost(final int count) {
        return new AtMost<>(count);
    }

    // TODO exactly, blocking
    static <S> SubscriptionMode<S, S> exactlyOnce() {
        return new ExactlyOnce<>();
    }

    // TODO exactly, blocking
    static <S> SubscriptionMode<S, List<S>> times(final int count) {
        return new Times<>(count);
    }

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, S> atLeastOnce() {
        return new AtLeastOnce<>();
    }

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, List<S>> atLeast(final int count) {
        return new AtLeast<>(count);
    }

}
