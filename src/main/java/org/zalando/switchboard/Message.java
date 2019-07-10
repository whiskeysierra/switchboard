package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
final class Message<T> implements Deliverable<T> {

    private final T message;

    @Override
    public <R> boolean satisfies(final Specification<R> specification) {
        final Class<R> type = specification.getMessageType();

        if (type.isInstance(message)) {
            return specification.test(type.cast(message));
        }

        return false;
    }

    @Override
    public void deliverTo(final Consumer<? super T> target) {
        target.accept(message);
    }

}
