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

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public interface Deliverable<T> {

    /**
     * Delivers this deliverable to the target. This is called on the receiving thread and allowed to fail.
     *
     * @param target target collection, potentially being passed to the receiver
     * @throws RuntimeException if delivery should fail
     */
    void deliverTo(Collection<? super T> target) throws ExecutionException;

    T getMessage();

    DeliveryMode getDeliveryMode();

    static <T> Deliverable<T> message(final T message, final DeliveryMode mode) {
        return new Message<>(message, mode);
    }

    static <T> Deliverable<T> failure(final T message, final DeliveryMode mode, final Throwable throwable) {
        return new Failure<>(message, mode, throwable);
    }

}
