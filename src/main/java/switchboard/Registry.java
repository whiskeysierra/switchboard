package switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

@ThreadSafe
interface Registry {

    <T, A> void register(Subscription<T, A> subscription);
    <T, A> Collection<Subscription<T, A>> find(Key<T, A> key);
    <T, A> void unregister(Subscription<T, A> subscription);

}
