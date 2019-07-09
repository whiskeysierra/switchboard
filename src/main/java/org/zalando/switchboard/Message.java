package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.util.Collection;

@AllArgsConstructor
final class Message<T> implements Deliverable<T> {

    private final T message;

    @Override
    public <R> boolean satisfies(final Specification<R> specification) {
        if (specification.getMessageType().isInstance(message)) {
            @SuppressWarnings("unchecked") final R typed = (R) this.message;
            return specification.test(typed);
        }
        return false;
    }

    @Override
    public void deliverTo(final Collection<? super T> target) {
        target.add(message);
    }

}
