package org.zalando.switchboard;

import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@AllArgsConstructor
public final class QueueAnsweringMachine implements AnsweringMachine {

    private final Collection<Deliverable<?>> queue;

    public QueueAnsweringMachine() {
        this(Collections.newSetFromMap(CacheBuilder.newBuilder()
                .maximumSize(100).<Deliverable<?>, Boolean>build().asMap()));
    }

    @Override
    public <T> void record(final Deliverable<T> deliverable) {
        queue.add(deliverable);
    }

    @Override
    public <T> Optional<Deliverable<T>> removeIf(final Specification<T> specification) {
        final var iterator = queue.iterator();

        while (iterator.hasNext()) {
            final Deliverable<?> deliverable = iterator.next();

            if (deliverable.satisfies(specification)) {
                iterator.remove();
                return Optional.of(cast(deliverable));
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> Deliverable<T> cast(final Deliverable<?> deliverable) {
        return (Deliverable<T>) deliverable;
    }

}
