package de.zalando.circuit;

import com.google.common.base.Predicate;

import javax.annotation.Nonnull;

public interface Subscription<E, H> extends Predicate<E> {

    Class<E> getEventType();

    Class<H> getHintType();

    @Override
    @SuppressWarnings("NullableProblems")
    boolean apply(@Nonnull E event);

    // TODO should be optional
    H getHint();

}
