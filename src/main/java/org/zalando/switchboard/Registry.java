package org.zalando.switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

@ThreadSafe
interface Registry {

    <T> void register(Subscription<T> subscription);
    <T> List<Subscription<T>> find(Deliverable<T> deliverable);
    <T> void unregister(Subscription<T> subscription);

}
