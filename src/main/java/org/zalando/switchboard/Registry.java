package org.zalando.switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

@ThreadSafe
interface Registry {

    <T, R> void register(Subscription<T, R> subscription);
    <T, R> List<Subscription<T, R>> find(Deliverable<T> deliverable);
    <T, R> void unregister(Subscription<T, R> subscription);

}
