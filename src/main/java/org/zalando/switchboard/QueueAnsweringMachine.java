package org.zalando.switchboard;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.Queue;

@AllArgsConstructor
public final class QueueAnsweringMachine implements AnsweringMachine {

    private final Queue<Deliverable<?>> queue;

    @SuppressWarnings("UnstableApiUsage")
    public QueueAnsweringMachine() {
        this(Queues.synchronizedQueue(EvictingQueue.create(1_000)));
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
