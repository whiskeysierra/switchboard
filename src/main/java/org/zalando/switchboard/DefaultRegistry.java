package org.zalando.switchboard;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import java.util.List;

import static java.util.stream.Collectors.toList;

public final class DefaultRegistry implements Registry {

    private final Multiset<Subscription<?, ?>> subscriptions = ConcurrentHashMultiset.create();

    @Override
    public <T, R> void register(final Subscription<T, R> subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public <T, R> List<Subscription<T, R>> find(final Deliverable<T> deliverable) {
        return subscriptions.stream()
                .filter(deliverable::satisfies)
                .map(this::<T, R>cast)
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
