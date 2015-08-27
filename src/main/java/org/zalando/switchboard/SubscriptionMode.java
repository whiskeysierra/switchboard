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

public interface SubscriptionMode<S, T, X extends Exception> {

    default boolean requiresTimeout() {
        return false;
    }

    T block(final Future<T> future, final long timeout, final TimeUnit timeoutUnit) throws X, InterruptedException, ExecutionException;

    boolean isDone(int received);

    boolean isSuccess(int received);

    T collect(List<S> results);

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, Void, RuntimeException> never() {
        return new Never<>();
    }

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, List<S>, RuntimeException> atMost(final int count) {
        return new AtMost<>(count);
    }

    // TODO exactly, blocking
    static <S> SubscriptionMode<S, S, TimeoutException> exactlyOnce() {
        return new ExactlyOnce<>();
    }

    // TODO exactly, blocking
    static <S> SubscriptionMode<S, List<S>, TimeoutException> times(final int count) {
        return new Times<>(count);
    }

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, S, TimeoutException> atLeastOnce() {
        return new AtLeastOnce<>();
    }

    // TODO non blocking, at most until end of timeout
    static <S> SubscriptionMode<S, List<S>, TimeoutException> atLeast(final int count) {
        return new AtLeast<>(count);
    }

}
