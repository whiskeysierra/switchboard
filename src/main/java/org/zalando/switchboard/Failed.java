package org.zalando.switchboard;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Failed<T, R> implements State<T, R> {

    private final Spec<T, R> spec;
    private final int received;

    @Override
    public R get() {
        // TODO custom exception type?!
        throw new IllegalStateException(spec.format(received));
    }

}
