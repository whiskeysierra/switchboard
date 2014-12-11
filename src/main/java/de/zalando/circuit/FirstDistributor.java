package de.zalando.circuit;

import java.util.List;

final class FirstDistributor implements Distributor {
        
    @Override
    public <E> List<Subscription<E, ?>> distribute(List<Subscription<E, ?>> subscriptions) {
        return subscriptions.subList(0, 1);
    }

}
