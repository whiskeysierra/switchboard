package switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

@ThreadSafe
interface Registry {

    <T, A> void register(Key<T, A> key, Subscription<T, A> subscription);
    <T, A> Collection<Subscription<T, A>> find(Deliverable<T, A> deliverable);
    <T, A> void unregister(Subscription<T, A> subscription);

}
