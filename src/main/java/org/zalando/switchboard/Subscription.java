package org.zalando.switchboard;

import javax.annotation.concurrent.Immutable;
import java.util.function.Predicate;

import static org.zalando.switchboard.TypeResolver.resolve;

@Immutable
@FunctionalInterface
public interface Subscription<E> extends Predicate<E> {

    default Class<E> getMessageType() {
        return resolve(this, Subscription.class, 0);
    }

    static <E> Subscription<E> on(final Class<E> messageType, final Predicate<E> predicate) {
        return new SimpleSubscription<>(messageType, predicate);
    }

}
