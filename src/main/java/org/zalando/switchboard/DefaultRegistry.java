package org.zalando.switchboard;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import java.util.List;

import static java.util.stream.Collectors.toList;

public final class DefaultRegistry implements Registry {

    private final Multiset<Subscription<?>> subscriptions = ConcurrentHashMultiset.create();

    @Override
    public <T> void register(final Subscription<T> subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public <T> List<Subscription<T>> find(final Deliverable<T> deliverable) {
        // TODO could this be made more efficient?
        return subscriptions.stream()
                .filter(deliverable::satisfies)
                .map(this::<T>cast)
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <T> Subscription<T> cast(final Subscription subscription) {
        return subscription;
    }

    @Override
    public <T> void unregister(final Subscription<T> subscription) {
        subscriptions.remove(subscription);
    }

}
