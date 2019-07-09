package org.zalando.switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received messages
 */
public interface Switchboard {

    <T> void send(Deliverable<T> deliverable);

    <T, R> R receive(Subscription<T> subscription, SubscriptionMode<T, R> mode, Duration timeout)
            throws ExecutionException, TimeoutException, InterruptedException;

    <T, R> Future<R> subscribe(Subscription<T> subscription, SubscriptionMode<T, R> mode);

    static Switchboard create() {
        return create(new QueueAnsweringMachine());
    }

    static Switchboard create(final AnsweringMachine machine) {
        return new DefaultSwitchboard(machine);
    }

}
