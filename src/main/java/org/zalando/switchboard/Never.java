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

import static java.lang.String.format;

final class Never<S> implements SubscriptionMode<S, Void, RuntimeException> {

    @Override
    public Void block(final Future<Void> future, final long timeout, final TimeUnit timeoutUnit)
            throws RuntimeException, InterruptedException, ExecutionException {
        try {
            return future.get(timeout, timeoutUnit);
        } catch (final TimeoutException e) {
            // expected
            return null;
        }
    }

    @Override
    public boolean isDone(final int received) {
        return received > 0;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received == 0;
    }

    @Override
    public String message(final String eventName, final int received, final long timeout, final String timeoutUnit) {
        return format("Expected no %s event, but got %d in %d %s", eventName, received, timeout, timeoutUnit);
    }

    @Override
    public Void collect(final List<S> results) {
        return null;
    }

}
