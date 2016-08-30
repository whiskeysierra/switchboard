package org.zalando.switchboard;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

final class Failure<T> implements Deliverable<T> {

    private final T message;
    private final DeliveryMode deliveryMode;
    private final Throwable throwable;

    Failure(final T message, final DeliveryMode deliveryMode, final Throwable throwable) {
        this.message = message;
        this.deliveryMode = deliveryMode;
        this.throwable = throwable;
    }

    @Override
    public void deliverTo(final Collection<? super T> target) throws ExecutionException {
        throw new ExecutionException(throwable);
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
