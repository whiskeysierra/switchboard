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

    <T, R> R receive(Subscription<T, ?> subscription, SubscriptionMode<T, R> mode, Duration timeout)
            throws ExecutionException, TimeoutException, InterruptedException;

    <T, R> Future<R> subscribe(Subscription<T, ?> subscription, SubscriptionMode<T, R> mode);

    <T, H> List<H> inspect(Class<T> messageType, Class<H> hintType);

    <T> void send(Deliverable<T> deliverable);

    static Switchboard create() {
        return new DefaultSwitchboard();
    }

}
