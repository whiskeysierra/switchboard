package de.zalando.circuit;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;

import static de.zalando.circuit.TypeResolver.resolve;

@FunctionalInterface
public interface Subscription<E, H> extends Predicate<E> {

    default Class<E> getEventType() {
        return resolve(this, Subscription.class, 0);
    }

    @Override
    boolean test(@Nonnull E e);
    
    default Optional<H> getHint() {
        return Optional.empty();
    }

}
