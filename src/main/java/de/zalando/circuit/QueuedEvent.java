package de.zalando.circuit;

import java.util.Collection;

final class QueuedEvent<T> implements Deliverable<T> {

    private final T event;
    private final DeliveryMode deliveryMode;

    QueuedEvent(T event, DeliveryMode deliveryMode) {
        this.event = event;
        this.deliveryMode = deliveryMode;
    }

    @Override
    public void sendTo(Circuit circuit) {
        circuit.send(event, deliveryMode);
    }

    @Override
    public void deliverTo(Collection<? super T> target) {
        target.add(event);
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
