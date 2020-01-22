package switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;

@ThreadSafe
interface AnsweringMachine {

    <T, A> void record(Deliverable<T, A> deliverable);
    <T, A> Optional<Deliverable<T, A>> removeIf(Key<T, A> key);

}
