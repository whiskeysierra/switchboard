package switchboard;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public final class DefaultRegistry implements Registry {

    private final Multimap<Key<?, ?>, Subscription<?, ?>> subscriptions =
            ConcurrentMultiMaps.newConcurrentMultiMap();

    @Override
    public <T, A> void register(
            final Key<T, A> key, final Subscription<T, A> subscription) {
        subscriptions.get(key).add(subscription);
    }

    @Override
    public <T, A> Collection<Subscription<T, A>> find(
            final Deliverable<T, A> deliverable) {

        return cast(subscriptions.get(deliverable.getKey()));
    }

    @SuppressWarnings("unchecked")
    private <T, A> Collection<Subscription<T, A>> cast(
            final Collection<? extends Subscription<?, ?>> result) {
        return (Collection<Subscription<T, A>>) result;
    }

    @Override
    public <T, A> void unregister(final Subscription<T, A> subscription) {
        subscriptions.get(subscription.getKey()).remove(subscription);
    }

}
