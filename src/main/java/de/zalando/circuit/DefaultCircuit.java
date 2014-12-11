package de.zalando.circuit;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class DefaultCircuit implements Circuit {
    
    @Override
    public <E> E receive(Subscription<E, ?> subscription, long timeout, TimeUnit timeoutUnit) throws TimeoutException {
        try {
            return subscribe(subscription).get(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            Thread.interrupted();
            // TODO better exception
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <E> Future<E> subscribe(Subscription<E, ?> subscription) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E, M> List<M> inspect(Class<E> eventType, Class<M> metadataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> void send(E event, Distribution distribution) {
        throw new UnsupportedOperationException();
    }

}
