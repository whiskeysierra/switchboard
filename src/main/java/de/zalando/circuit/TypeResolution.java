package de.zalando.circuit;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkState;

final class TypeResolution {

    static <T> Class<T> resolve(final Class<?> type, int index) {
        final Type genericType = type.getGenericSuperclass();
        checkState(genericType instanceof ParameterizedType, 
                "%s is neither generic nor does it override Subscription.getType", type.getSimpleName());
        final ParameterizedType parameterizedType = (ParameterizedType) genericType;
        @SuppressWarnings("unchecked")
        final Class<T> typeArgument = (Class<T>) parameterizedType.getActualTypeArguments()[index];
        return typeArgument;
    }

}
