package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.util.concurrent.TimeoutException;

@AllArgsConstructor
final class TimedOut<T, R> implements State<T, R> {

    private final Spec<T, R> spec;
    private final int received;

    @Override
    public R get() throws TimeoutException {
        throw new TimeoutException(spec.format(received));
    }

}
