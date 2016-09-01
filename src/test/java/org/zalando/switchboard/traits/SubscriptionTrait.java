package org.zalando.switchboard.traits;

import org.zalando.switchboard.Subscription;

public interface SubscriptionTrait<S> {

    Subscription<S, ?> matchA();

    default Subscription<S, ?> matchB() {
        return e -> false;
    }

    S messageA();

}
