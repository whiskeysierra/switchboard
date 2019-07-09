package org.zalando.switchboard;

import java.util.Optional;
import java.util.function.Predicate;

interface AnsweringMachine {

    <T> void record(Deliverable<T> deliverable);

    <T> Optional<Deliverable<T>> removeIf(Predicate<Object> predicate);

}
