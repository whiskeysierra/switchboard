package org.zalando.switchboard;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Optional;
import java.util.function.Predicate;

import static org.zalando.switchboard.TypeResolver.resolve;

@Immutable
@FunctionalInterface
public interface Subscription<E, H> extends Predicate<E> {

    default Class<E> getMessageType() {
        return resolve(this, Subscription.class, 0);
    }

    @Override
    boolean test(@Nonnull E e);

    default Optional<H> getHint() {
        return Optional.empty();
    }

    static <E, H> Subscription<E, H> on(final Class<E> messageType, final Predicate<E> predicate) {
        return new SimpleSubscription<>(messageType, predicate, Optional.empty());
    }

    static <E, H> Subscription<E, H> on(final Class<E> messageType, final Predicate<E> predicate, final H hint) {
        return new SimpleSubscription<>(messageType, predicate, Optional.of(hint));
    }

}
