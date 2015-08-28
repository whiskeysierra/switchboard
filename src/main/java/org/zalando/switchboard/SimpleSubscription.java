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

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;

final class SimpleSubscription<T, H> implements Subscription<T, H> {

    private final Class<T> messageType;
    private final Predicate<T> predicate;
    private final Optional<H> hint;

    SimpleSubscription(final Class<T> messageType, final Predicate<T> predicate, final Optional<H> hint) {
        this.messageType = messageType;
        this.predicate = predicate;
        this.hint = hint;
    }

    @Override
    public Class<T> getMessageType() {
        return messageType;
    }

    @Override
    public boolean test(@Nonnull final T t) {
        return predicate.test(t);
    }

    @Override
    public Optional<H> getHint() {
        return hint;
    }

}
