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

final class SimpleSubscription<E, H> implements Subscription<E, H> {

    private final Class<E> eventType;
    private final Predicate<E> predicate;
    private final Optional<H> hint;

    public SimpleSubscription(Class<E> eventType, Predicate<E> predicate, H hint) {
        this.eventType = eventType;
        this.predicate = predicate;
        this.hint = Optional.of(hint);
    }

    @Override
    public Class<E> getEventType() {
        return eventType;
    }

    @Override
    public boolean test(@Nonnull E e) {
        return predicate.test(e);
    }

    @Override
    public Optional<H> getHint() {
        return hint;
    }

}
