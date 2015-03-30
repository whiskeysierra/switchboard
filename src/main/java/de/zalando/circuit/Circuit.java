package de.zalando.circuit;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * publish/subscribe, asnyc, hand-over/broadcast
 * deliver previously received events
 */
public interface Circuit {

    <E> E receive(Subscription<E, ?> subscription, long timeout, TimeUnit timeoutUnit) throws TimeoutException;

    <E> List<E> receive(Subscription<E, ?> subscription, final int count, long timeout, TimeUnit timeoutUnit) throws TimeoutException;

    <E> Future<E> subscribe(Subscription<E, ?> subscription);

    <E> Future<List<E>> subscribe(Subscription<E, ?> subscription, final int count);

    <E, H> List<H> inspect(Class<E> eventType, Class<H> hintType);

    <E> void send(E event, Distribution distribution);
    
    <E> void fail(E event, Distribution distribution, RuntimeException exception);
    
}
