package org.zalando.switchboard;

import java.util.function.Consumer;

public interface Deliverable<T> {

    /**
     * Delivers this deliverable to the target. This is called on the receiving thread and allowed to fail.
     *
     * @param target target collection, potentially being passed to the receiver
     * @throws RuntimeException if delivery should fail
     */
    void deliverTo(Consumer<? super T> target);

    <R> boolean satisfies(Specification<R> specification);

    static <T> Deliverable<T> message(final T message) {
        return new Message<>(message);
    }

}
