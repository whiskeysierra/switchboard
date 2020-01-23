package switchboard;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Failed<T, R> implements State<T, R> {

    private final String message;

    @Override
    public R get() {
        throw new IllegalStateException(message);
    }

}
