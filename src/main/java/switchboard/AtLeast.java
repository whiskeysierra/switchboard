package switchboard;

import lombok.AllArgsConstructor;

import java.util.Collection;

@AllArgsConstructor
final class AtLeast<S> implements SubscriptionMode<S, Collection<S>> {

    private final int count;

    @Override
    public boolean isDone(final int received) {
        return received >= count;
    }

    @Override
    public boolean isSuccess(final int received) {
        return received >= count;
    }

    @Override
    public Collection<S> transform(final Collection<S> results) {
        return results;
    }

    @Override
    public String toString() {
        return "at least " + count;
    }

}
