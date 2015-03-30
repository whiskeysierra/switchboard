package de.zalando.circuit;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Futures;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;

/**
 * publish/subscribe, asnyc, hand-over/broadcast
 * deliver previously received events
 */
public interface Circuit {

    default <E> E receive(Subscription<E, ?> subscription, long timeout, TimeUnit timeoutUnit) throws TimeoutException {
        return receive(subscription, 1, timeout, timeoutUnit).get(0);
    }

    default <E> List<E> receive(Subscription<E, ?> subscription, int count, long timeout, TimeUnit timeoutUnit) throws TimeoutException {
        try {
            return subscribe(subscription, count).get(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // TODO is throwing an exception here the right thing?
            throw new RuntimeException("Thread has been interrupted while waiting");
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(e.getCause());
            throw new IllegalStateException(e.getCause());
        }
    }

    default <E> Future<E> subscribe(Subscription<E, ?> subscription) {
        return Futures.lazyTransform(subscribe(subscription, 1),
                (List<E> list) -> list.isEmpty() ? null : list.get(0));
    }

    <E> Future<List<E>> subscribe(Subscription<E, ?> subscription, int count);

    default <E> void unless(Subscription<E, ?> subscription, long timeout, TimeUnit timeoutUnit) {
        unless(subscription, 1, timeout, timeoutUnit);
    }

    default <E> void unless(Subscription<E, ?> subscription, int count, long timeout, TimeUnit timeoutUnit) {
        try {
            receive(subscription, count, timeout, timeoutUnit);

            final String typeName = subscription.getEventType().getSimpleName();
            final String timeoutUnitName = timeoutUnit.name().toLowerCase(Locale.ENGLISH);
            throw new IllegalStateException(format("Didn't expect [%s] event matching [%s] in [%s] [%s]",
                    typeName, subscription, timeout, timeoutUnitName));
        } catch (TimeoutException e) {
            // expected
        }
    }

    <E, H> List<H> inspect(Class<E> eventType, Class<H> hintType);

    <E> void send(E event, DeliveryMode deliveryMode);

    <E> void fail(E event, DeliveryMode deliveryMode, RuntimeException exception);

}
