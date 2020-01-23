package switchboard;

import lombok.AllArgsConstructor;

import java.util.concurrent.TimeoutException;

@AllArgsConstructor
final class TimedOut<T, R> implements State<T, R> {

    private final String message;

    @Override
    public R get() throws TimeoutException {
        throw new TimeoutException(message);
    }

}
