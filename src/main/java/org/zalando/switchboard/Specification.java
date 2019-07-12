package org.zalando.switchboard;

import javax.annotation.concurrent.Immutable;
import java.time.Instant;
import java.util.function.Predicate;

import static org.zalando.switchboard.TypeResolver.resolve;

// TODO rename to Query?
@Immutable
@FunctionalInterface
public interface Specification<T> extends Predicate<T> {

    default Class<T> getMessageType() {
        return resolve(this, Specification.class, 0);
    }

    static <T> Specification<T> on(final Class<T> messageType, final Predicate<T> predicate) {
        return new SimpleSpecification<>(messageType, predicate);
    }

}
