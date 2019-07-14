package org.zalando.switchboard;

public interface Deliverable<T> {

    // TODO if we wrap specifications and apply type check we could get rid of this interface completely
    <R> boolean satisfies(Specification<R> specification);

    T getMessage();

    static <T> Deliverable<T> message(final T message) {
        return new Message<>(message);
    }

}
