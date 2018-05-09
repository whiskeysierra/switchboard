package org.zalando.switchboard;

import com.google.common.reflect.TypeToken;

final class TypeResolver {

    private TypeResolver() {

    }

    static <T> Class<T> resolve(final Object instance, final Class<?> type, final int index) {
        final TypeToken<?> token = TypeToken.of(instance.getClass());
        final TypeToken<?> resolved = token.resolveType(type.getTypeParameters()[index]);
        return cast(resolved.getRawType());
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> cast(final Class<?> type) {
        return (Class<T>) type;
    }

}
