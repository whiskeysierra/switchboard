package org.zalando.switchboard;

import java.util.List;

final class Broadcast implements DeliveryMode {

    static final DeliveryMode INSTANCE = new Broadcast();

    private Broadcast() {
        // singleton
    }

    @Override
    public <S, T> List<Subscription<S, T>> distribute(final List<Subscription<S, T>> deliveries) {
        return deliveries;
    }

    @Override
    public String toString() {
        return "broadcast()";
    }

}
