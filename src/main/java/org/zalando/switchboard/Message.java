package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
final class Message<T> implements Deliverable<T> {

    @Getter
    private final T message;

    @Override
    public <R> boolean satisfies(final Specification<R> specification) {
        final var type = specification.getMessageType();

        if (type.isInstance(message)) {
            return specification.test(type.cast(message));
        }

        return false;
    }

}
