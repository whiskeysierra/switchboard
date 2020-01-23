package switchboard;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static switchboard.ConcurrentMultimaps.newConcurrentMultiMap;

class ConcurrentMultimapsTest {

    @Test
    void supportsIteratorTraversal() {
        final SetMultimap<String, String> map = newConcurrentMultiMap();
        map.putAll("key", asList("v1", "v2", "v3"));

        final var values = map.get("key");
        final var iterator = values.iterator();

        assertTrue(iterator.hasNext());
        values.clear();
        assertEquals("v1", iterator.next());
    }

}
