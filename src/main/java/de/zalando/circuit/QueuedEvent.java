package de.zalando.circuit;

final class QueuedEvent<T> {

    private final T original;
    private final Distributor distributor;

    QueuedEvent(T original, Distributor distributor) {
        this.original = original;
        this.distributor = distributor;
    }

    public T getOriginal() {
        return original;
    }

    public Distributor getDistributor() {
        return distributor;
    }
    
}
