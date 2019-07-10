package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Optional;

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
    public void deliverTo(final Collection<? super T> target) {
        target.add(message);
    }

}
