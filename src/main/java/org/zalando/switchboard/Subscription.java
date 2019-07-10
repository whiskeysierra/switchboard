package org.zalando.switchboard;

public interface Subscription<T, R> extends Specification<T>, Promise<R> {

    void deliver(Deliverable<T> deliverable);

}
