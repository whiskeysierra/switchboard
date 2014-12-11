package de.zalando.circuit;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.atomic.AtomicReference;

final class Delivery<E, M> implements Predicate<Object> {

    enum State {
        WAITING, DONE, FAILED
    }
    
    private final AtomicReference<State> state = new AtomicReference<>(State.WAITING);
    
    private final SettableFuture<E> future = SettableFuture.create();
    private final Subscription<E, M> subscription;

    Delivery(Subscription<E, M> subscription) {
        this.subscription = subscription;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean apply(Object input) {
        return subscription.getType().isInstance(input)
                && subscription.apply((E) input);
    }
    
    void deliver(E event) {
        future.set(event);
    }
    
}
