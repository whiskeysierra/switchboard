package org.zalando.switchboard;

public interface Deliverable<T> {

    <R> boolean satisfies(Specification<R> specification);

    T getMessage();

    static <T> Deliverable<T> message(final T message) {
        return new Message<>(message);
    }

}
