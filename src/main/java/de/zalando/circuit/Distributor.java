package de.zalando.circuit;

import java.util.List;

public interface Distributor {
    
    Distributor DIRECT = new DirectDistributor();
    Distributor FIRST = new FirstDistributor();
    Distributor BROADCAST = new BroadcastDistributor();

    // TODO is the order of given subscriptions defined? 
    <E> List<Subscription<E, ?>> distribute(List<Subscription<E, ?>> subscriptions);

}
