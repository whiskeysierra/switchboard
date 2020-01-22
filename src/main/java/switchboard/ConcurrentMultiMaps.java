package switchboard;

import com.google.common.collect.SetMultimap;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Multimaps.newSetMultimap;

final class ConcurrentMultiMaps {

    private ConcurrentMultiMaps() {
        // nothing to do
    }

    static <K, V> SetMultimap<K, V> newConcurrentMultiMap() {
        final var map = new ConcurrentHashMap<K, Collection<V>>();
        return newSetMultimap(map, ConcurrentHashMap::newKeySet);
    }

}
