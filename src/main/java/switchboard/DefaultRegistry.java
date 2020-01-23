package switchboard;

import com.google.common.collect.Multimap;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

public final class DefaultRegistry implements Registry {

    private final Multimap<Key<?, ?>, Subscription<?, ?>> subscriptions =
            ConcurrentMultimaps.newConcurrentMultiMap();

    @Override
    public <T, A> void register(
            final Subscription<T, A> subscription) {
        subscriptions.get(subscription.getKey()).add(subscription);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, A> Collection<Subscription<T, A>> find(final Key<T, A> key) {
        final Collection<? extends Subscription<?, ?>> match =
                subscriptions.get(key);
        return unmodifiableCollection((Collection<Subscription<T, A>>) match);
    }

    @Override
    public <T, A> void unregister(final Subscription<T, A> subscription) {
        subscriptions.get(subscription.getKey()).remove(subscription);
    }

}
