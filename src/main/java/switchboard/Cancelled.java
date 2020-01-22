package switchboard;

import java.util.concurrent.CancellationException;

final class Cancelled<T, R> implements State<T, R> {

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public R get() {
        throw new CancellationException();
    }

}
