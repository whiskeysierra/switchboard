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

import com.google.common.reflect.TypeToken;

final class TypeResolver {
    
    TypeResolver() {
        // package private so we can trick code coverage
    }

    static <T> Class<T> resolve(final Object instance, final Class<?> type, int index) {
        final TypeToken<?> token = TypeToken.of(instance.getClass());
        final TypeToken<?> resolved = token.resolveType(type.getTypeParameters()[index]);
        return cast(resolved.getRawType());
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> cast(final Class<?> type) {
        return (Class<T>) type;
    }

}
