package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@AllArgsConstructor
public final class QueueAnsweringMachine implements AnsweringMachine {

    private final Queue<Deliverable<?>> queue;

    public QueueAnsweringMachine() {
        this(new LinkedBlockingQueue<>(1_000));
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
