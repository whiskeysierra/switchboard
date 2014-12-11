package de.zalando.circuit;

final class QueuedEvent<T> {

    private final T original;
    private final Distribution distribution;

    QueuedEvent(T original, Distribution distribution) {
        this.original = original;
        this.distribution = distribution;
    }

    void deliverTo(Circuit circuit) {
        circuit.send(original, distribution);
    }

    public T getOriginal() {
        return original;
    }

}
