package de.zalando.circuit;

public interface Subscription<E, M> {

    Class<E> getType();
    
    boolean matches(E message);

    M getMetadata();

}
