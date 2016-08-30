package org.zalando.switchboard;

import java.util.Collection;

final class Message<T> implements Deliverable<T> {

    private final T message;
    private final DeliveryMode deliveryMode;

    Message(final T message, final DeliveryMode deliveryMode) {
        this.message = message;
        this.deliveryMode = deliveryMode;
    }

    @Override
    public void deliverTo(final Collection<? super T> target) {
        target.add(message);
    }

    @Override
    public T getMessage() {
        return message;
    }

    @Override
    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }
    
}
