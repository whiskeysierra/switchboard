package org.zalando.switchboard;

import java.util.List;

public interface DeliveryMode {

    <S, T> List<Subscription<S, T>> distribute(List<Subscription<S, T>> deliveries);

    static DeliveryMode directly() {
        return Direct.INSTANCE;
    }

    static DeliveryMode broadcast() {
        return Broadcast.INSTANCE;
    }

    static DeliveryMode first() {
        return First.INSTANCE;
    }

}
