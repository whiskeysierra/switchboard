package org.zalando.switchboard;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Promise<V> {

    boolean cancel(boolean mayInterruptIfRunning);
    boolean isCancelled();
    boolean isDone();
    V get(long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException;

}
