package org.zalando.switchboard;

import java.util.Optional;

public final class NoAnsweringMachine implements AnsweringMachine {

    @Override
    public <T> void record(final Deliverable<T> deliverable) {
        // nothing to do
    }

    @Override
    public <T> Optional<Deliverable<T>> removeIf(final Specification<T> specification) {
        return Optional.empty();
    }

}
