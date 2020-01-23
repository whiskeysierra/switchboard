package switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

@ThreadSafe
interface AnsweringMachine {

    <T, A> void record(Deliverable<T, A> deliverable);
    <T, A> Collection<Deliverable<T, A>> listen(Key<T, A> key);

}
