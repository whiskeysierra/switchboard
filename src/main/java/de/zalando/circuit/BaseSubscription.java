package de.zalando.circuit;

import static de.zalando.circuit.TypeResolver.resolve;

public abstract class BaseSubscription<E, H> implements Subscription<E, H> {

    @Override
    public Class<E> getEventType() {
        return resolve(this, Subscription.class, 0);
    }

    @Override
    public Class<H> getHintType() {
        return resolve(this, Subscription.class, 1);
    }

}
