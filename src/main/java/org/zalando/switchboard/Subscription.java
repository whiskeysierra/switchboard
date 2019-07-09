package org.zalando.switchboard;

import java.util.concurrent.Future;

public interface Subscription<T, R> extends Future<R>, Specification<T> {

    void deliver(final Deliverable<T> deliverable);

}
