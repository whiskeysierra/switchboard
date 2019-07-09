package org.zalando.switchboard;

import java.util.List;

public interface DeliveryMode {

    // TODO the type Answer is not public and shouldn't be used in an interface!
    // TODO is the order of given subscriptions defined?
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
