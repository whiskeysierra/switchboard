package switchboard;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.K;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor
public final class InMemoryAnsweringMachine implements AnsweringMachine {

    private final Multimap<Key<?, ?>, Deliverable<?, ?>> queue;

    public InMemoryAnsweringMachine() {
        this(ConcurrentMultiMaps.newConcurrentMultiMap());
    }

    @Override
    public <T, A> void record(final Deliverable<T, A> deliverable) {
        queue.get(deliverable.getKey()).add(deliverable);
    }

    @Override
    public <T, A> Optional<Deliverable<T, A>> removeIf(final Key<T, A> key) {
        final var deliverables = queue.get(key);

        final var iterator = deliverables.iterator();

        if (iterator.hasNext()) {
            // TODO race condition: queue might be empty by now
            final var deliverable = iterator.next();
            iterator.remove();
            return Optional.of(cast(deliverable));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T, A> Deliverable<T, A> cast(final Deliverable<?, ?> deliverable) {
        return (Deliverable<T, A>) deliverable;
    }

}
