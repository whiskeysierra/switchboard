package switchboard.traits;

import switchboard.Key;

public interface SubscriptionTrait<S, A> {

    Key<S, A> matchA();
    Key<S, A> matchB();

    S messageA();
    S messageB();

}
