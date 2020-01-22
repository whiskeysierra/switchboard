package switchboard;

import java.util.Collection;

final class AtLeastOnce<S> implements SubscriptionMode<S, S> {

    @Override
    public boolean isDoneEarly(final int received) {
        return received >= 1;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received >= 1;
    }

    @Override
    public S transform(final Collection<S> results) {
        return results.iterator().next();
    }

    @Override
    public String toString() {
        return "at least one";
    }

}
