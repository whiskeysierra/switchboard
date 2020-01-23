package switchboard;

import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.SetMultimap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.Multimaps.newSetMultimap;
import static java.util.Collections.newSetFromMap;

final class ConcurrentMultimaps {

    private ConcurrentMultimaps() {
        // nothing to do
    }

    static <K, V> SetMultimap<K, V> newConcurrentMultiMap() {
        return newSetMultimap(
                new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
    }

    static <K, V> SetMultimap<K, V> newConcurrentMultiMap(
            final UnaryOperator<CacheBuilder<Object, Object>> operator) {
        return newSetMultimap(
                new ConcurrentHashMap<>(), () -> newKeySet(operator));
    }

    private static <V> Set<V> newKeySet(
            final UnaryOperator<CacheBuilder<Object, Object>> operator) {
        final var cache = operator.apply(newBuilder()).<V, Boolean>build();
        return newSetFromMap(cache.asMap());
    }

}
