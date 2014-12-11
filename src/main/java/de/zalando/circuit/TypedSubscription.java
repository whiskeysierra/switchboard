package de.zalando.circuit;

import static com.google.common.base.Preconditions.checkState;

public abstract class TypedSubscription<E, M> implements Subscription<E, M> {

    @Override
    public Class<E> getType() {
        return TypeResolution.resolve(getClass(), 0);
    }

}
