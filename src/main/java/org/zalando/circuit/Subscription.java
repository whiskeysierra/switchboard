package org.zalando.circuit;

/*
 * ⁣​
 * Circuit
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

import static org.zalando.circuit.TypeResolver.resolve;

@FunctionalInterface
public interface Subscription<E, H> extends Predicate<E> {

    default Class<E> getEventType() {
        return resolve(this, Subscription.class, 0);
    }

    @Override
    boolean test(@Nonnull E e);
    
    default Optional<H> getHint() {
        return Optional.empty();
    }

}
