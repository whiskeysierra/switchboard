package de.zalando.circuit;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * publish/subscribe, asnyc, hand-over
 * deliver previously received events
 */
public interface Circuit {

    <E> E receive(Subscription<E, ?> subscription, long timeout, TimeUnit timeoutUnit) throws TimeoutException;

    <E> Future<E> subscribe(Subscription<E, ?> subscription);
    
    <E, M> List<M> inspect(Class<E> eventType, Class<M> metadataType);

    <E> void send(E event, Distribution distribution);
    
}
