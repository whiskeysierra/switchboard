package de.zalando.circuit;

import java.util.Collection;

final class QueuedError<T> implements Deliverable<T> {

    private final T event;
    private final DeliveryMode deliveryMode;
    private final RuntimeException exception;

    QueuedError(T event, DeliveryMode deliveryMode, RuntimeException exception) {
        this.event = event;
        this.deliveryMode = deliveryMode;
        this.exception = exception;
    }

    @Override
    public void sendTo(Circuit circuit) {
        circuit.fail(event, deliveryMode, exception);
    }

    @Override
    public void deliverTo(Collection<? super T> target) {
        throw exception;
    }

    @Override
    public T getEvent() {
        return event;
    }

    @Override
    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

}
