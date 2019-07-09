package org.zalando.switchboard;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class QueueAnsweringMachine implements AnsweringMachine {

    private final Queue<Deliverable<?>> queue = new ConcurrentLinkedQueue<>();

    @Override
    public <T> void record(final Deliverable<T> deliverable) {
        queue.add(deliverable);
    }

    @Override
    public <T> Optional<Deliverable<T>> removeIf(final Specification<T> specification) {
        final var iterator = queue.iterator();

        while (iterator.hasNext()) {
            final Deliverable<?> raw = iterator.next();

            if (specification.getMessageType().isInstance(raw.getMessage())) {
                final Deliverable<T> deliverable = cast(raw);

                if (specification.test(deliverable.getMessage())) {
                    iterator.remove();
                    return Optional.of(deliverable);
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> Deliverable<T> cast(final Deliverable deliverable) {
        return deliverable;
    }

}
