package switchboard;

import lombok.AllArgsConstructor;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

@AllArgsConstructor
final class Success<T, R> implements State<T, R> {

    private final SubscriptionMode<T, R> mode;
    private final Collection<T> queue;

    @Override
    public R get() {
        return mode.transform(unmodifiableCollection(queue));
    }

}
