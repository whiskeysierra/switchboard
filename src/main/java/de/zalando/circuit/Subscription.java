package de.zalando.circuit;

import com.google.common.base.Predicate;

public interface Subscription<E, M> extends Predicate<E> {

    Class<E> getType();
    
    M getMetadata();

}
