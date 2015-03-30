package de.zalando.circuit;

import java.util.Collection;

interface Deliverable<E> {

    void sendTo(Circuit circuit);
    
    void deliverTo(Collection<? super E> target);

    E getEvent();

    Distribution getDistribution();
}
