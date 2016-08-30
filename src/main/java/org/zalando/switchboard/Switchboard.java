package org.zalando.switchboard;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received messages
 */
public interface Switchboard {


    <T, R> R receive(final Subscription<T, ?> subscription, final SubscriptionMode<T, R> mode, final Duration timeout)
            throws ExecutionException, TimeoutException, InterruptedException;

    <T, R> Future<R> subscribe(final Subscription<T, ?> subscription, final SubscriptionMode<T, R> mode);

    <T, H> List<H> inspect(final Class<T> messageType, final Class<H> hintType);

    <T> void send(final Deliverable<T> deliverable);

    static Switchboard create() {
        return new DefaultSwitchboard();
    }

}
