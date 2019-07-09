package org.zalando.switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;

@ThreadSafe
interface AnsweringMachine {

    <T> void record(Deliverable<T> deliverable);
    <T> Optional<Deliverable<T>> removeIf(Specification<T> specification);

}
