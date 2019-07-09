package org.zalando.switchboard;

import javax.annotation.concurrent.Immutable;
import java.util.function.Predicate;

import static org.zalando.switchboard.TypeResolver.resolve;

@Immutable
@FunctionalInterface
public interface Specification<T> extends Predicate<T> {

    default Class<T> getMessageType() {
        return resolve(this, Specification.class, 0);
    }

    static <E> Specification<E> on(final Class<E> messageType, final Predicate<E> predicate) {
        return new SimpleSpecification<>(messageType, predicate);
    }

}
