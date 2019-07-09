package org.zalando.switchboard;

import java.util.concurrent.Future;

public interface Subscription<T, R> extends Specification<T>, Future<R> {

    void deliver(Deliverable<T> deliverable);

}
