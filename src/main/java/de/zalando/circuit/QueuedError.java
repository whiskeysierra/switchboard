package de.zalando.circuit;

import java.util.Collection;

final class QueuedError<T> implements Deliverable<T> {

    private final T event;
    private final Distribution distribution;
    private final RuntimeException exception;

    QueuedError(T event, Distribution distribution, RuntimeException exception) {
        this.event = event;
        this.distribution = distribution;
        this.exception = exception;
    }

    @Override
    public void sendTo(Circuit circuit) {
        circuit.fail(event, distribution, exception);
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
    public Distribution getDistribution() {
        return distribution;
    }

}
