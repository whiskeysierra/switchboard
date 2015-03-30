package de.zalando.circuit;

import java.util.Collection;

final class QueuedEvent<T> implements Deliverable<T> {

    private final T event;
    private final Distribution distribution;

    QueuedEvent(T event, Distribution distribution) {
        this.event = event;
        this.distribution = distribution;
    }

    @Override
    public void sendTo(Circuit circuit) {
        circuit.send(event, distribution);
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
    public Distribution getDistribution() {
        return distribution;
    }
    
}
