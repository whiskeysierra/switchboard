package org.zalando.switchboard;

import java.util.List;

public interface DeliveryMode {

    <S, T> List<Answer<S, T>> distribute(List<Answer<S, T>> deliveries);

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
