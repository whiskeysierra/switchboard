package de.zalando.circuit;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

final class DirectDistributor implements Distributor {
        
    @Override
    public <E> List<Subscription<E, ?>> distribute(List<Subscription<E, ?>> subscriptions) {
        checkState(subscriptions.size() == 1, "Too many subcriptions for event, expected one");
        return subscriptions.subList(0, 1);
    }

}
