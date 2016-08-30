package org.zalando.switchboard;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;

final class SimpleSubscription<T, H> implements Subscription<T, H> {

    private final Class<T> messageType;
    private final Predicate<T> predicate;
    private final Optional<H> hint;

    SimpleSubscription(final Class<T> messageType, final Predicate<T> predicate, final Optional<H> hint) {
        this.messageType = messageType;
        this.predicate = predicate;
        this.hint = hint;
    }

    @Override
    public Class<T> getMessageType() {
        return messageType;
    }

    @Override
    public boolean test(@Nonnull final T t) {
        return predicate.test(t);
    }

    @Override
    public Optional<H> getHint() {
        return hint;
    }

}
