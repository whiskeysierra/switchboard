package org.zalando.switchboard;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

// TODO package private?
public final class QueueAnsweringMachine implements AnsweringMachine {

    private final Queue<Deliverable<?>> queue = new ConcurrentLinkedQueue<>();

    @Override
    public <T> void record(final Deliverable<T> deliverable) {
        queue.add(deliverable);
    }

    @Override
    public <T> Optional<Deliverable<T>> removeIf(final Predicate<Object> predicate) {
        final var iterator = queue.iterator();

        while (iterator.hasNext()) {
            final Deliverable deliverable = iterator.next();

            if (predicate.test(deliverable.getMessage())) {
                iterator.remove();
                return Optional.of(cast(deliverable));
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> Deliverable<T> cast(final Deliverable deliverable) {
        return deliverable;
    }

}
