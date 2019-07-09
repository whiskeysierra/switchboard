package org.zalando.switchboard;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

final class SimpleSpecification<T> implements Specification<T> {

    private final Class<T> messageType;
    private final Predicate<T> predicate;

    SimpleSpecification(final Class<T> messageType, final Predicate<T> predicate) {
        this.messageType = messageType;
        this.predicate = predicate;
    }

    @Override
    public Class<T> getMessageType() {
        return messageType;
    }

    @Override
    public boolean test(@Nonnull final T t) {
        return predicate.test(t);
    }

}
