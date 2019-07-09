package org.zalando.switchboard;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.stream.Collectors.toList;

public final class QueueSubscriptions implements Subscriptions {

    private final Queue<Subscription> subscriptions = new ConcurrentLinkedQueue<>();

    @Override
    public <T, R> void register(final Subscription<T, R> subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public <T, R> List<Subscription<T, R>> find(final T message) {
        return subscriptions.stream()
            .map(this::<T, R>cast)
            .filter(subscription -> subscription.getMessageType().isInstance(message))
            .filter(subscription -> subscription.test(message))
            .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <T, R> Subscription<T, R> cast(final Subscription subscription) {
        return subscription;
    }

    @Override
    public <T, R> void unregister(final Subscription<T, R> subscription) {
        subscriptions.remove(subscription);
    }

}
