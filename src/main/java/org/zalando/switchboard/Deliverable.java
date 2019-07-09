package org.zalando.switchboard;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public interface Deliverable<T> {

    /**
     * Delivers this deliverable to the target. This is called on the receiving thread and allowed to fail.
     *
     * @param target target collection, potentially being passed to the receiver
     * @throws RuntimeException if delivery should fail
     */
    void deliverTo(Collection<? super T> target);

    T getMessage();

    DeliveryMode getDeliveryMode();

    static <T> Deliverable<T> message(final T message, final DeliveryMode mode) {
        return new Message<>(message, mode);
    }

}
