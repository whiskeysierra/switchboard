package de.zalando.circuit;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkState;

public abstract class TypedSubscription<E, M> implements Subscription<E, M> {

    @Override
    public Class<E> getType() {
        final Class<? extends TypedSubscription> type = getClass();
        final Type genericType = type.getGenericSuperclass();
        checkState(genericType instanceof ParameterizedType, 
                "%s is neither generic nor does it override Subscription.getType", type.getSimpleName());
        final ParameterizedType parameterizedType = (ParameterizedType) genericType;
        @SuppressWarnings("unchecked")
        final Class<E> typeArgument = (Class<E>) parameterizedType.getActualTypeArguments()[0];
        return typeArgument;

    }

}
