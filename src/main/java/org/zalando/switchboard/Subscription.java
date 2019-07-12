package org.zalando.switchboard;

public interface Subscription<T> extends Specification<T> {

    boolean deliver(Deliverable<T> deliverable);

}
