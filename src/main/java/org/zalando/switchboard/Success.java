package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

@AllArgsConstructor
final class Success<T, R> implements State<T, R> {

    private final SubscriptionMode<T, R> spec;
    private final Collection<T> queue;

    @Override
    public R get() {
        return spec.transform(unmodifiableCollection(queue));
    }

}
