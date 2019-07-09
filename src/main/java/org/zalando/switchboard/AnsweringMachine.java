package org.zalando.switchboard;

import java.util.Optional;

interface AnsweringMachine {

    <T> void record(Deliverable<T> deliverable);
    <T> Optional<Deliverable<T>> removeIf(Specification<T> specification);

}
