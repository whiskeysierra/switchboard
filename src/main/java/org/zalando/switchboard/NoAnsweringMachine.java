package org.zalando.switchboard;

import java.util.Optional;
import java.util.function.Predicate;

public final class NoAnsweringMachine implements AnsweringMachine {

    @Override
    public <T> void record(final Deliverable<T> deliverable) {
        // nothing to do
    }

    @Override
    public <T> Optional<Deliverable<T>> removeIf(final Predicate<Object> predicate) {
        return Optional.empty();
    }

}
