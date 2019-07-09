package org.zalando.switchboard;

import java.util.concurrent.Future;
import java.util.function.Predicate;

public interface Answer<T, R> extends Future<R>, Predicate<Object> {
    Class<T> getMessageType();

    void deliver(Deliverable<T> deliverable);
}
