package switchboard;

import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import java.time.Duration;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static switchboard.ConcurrentMultimaps.newConcurrentMultiMap;

@API(status = EXPERIMENTAL)
@AllArgsConstructor(access = PRIVATE)
public final class InMemoryAnsweringMachine implements AnsweringMachine {

    private final Multimap<Key<?, ?>, Deliverable<?, ?>> deliverables;

    public InMemoryAnsweringMachine(final Duration expiration) {
        this(newConcurrentMultiMap(builder -> builder
                        .expireAfterWrite(expiration)));
    }

    @Override
    public <T, A> void record(final Deliverable<T, A> deliverable) {
        deliverables.get(deliverable.getKey()).add(deliverable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, A> Collection<Deliverable<T, A>> listen(final Key<T, A> key) {
        final Collection<? extends Deliverable<?, ?>> match =
                deliverables.get(key);
        return unmodifiableCollection((Collection<Deliverable<T, A>>) match);
    }

}
