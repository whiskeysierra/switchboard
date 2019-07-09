package org.zalando.switchboard.traits;

import org.zalando.switchboard.Specification;

public interface SubscriptionTrait<S> {

    Specification<S> matchA();

    default Specification<S> matchB() {
        return e -> false;
    }

    S messageA();
    S messageB();

}
