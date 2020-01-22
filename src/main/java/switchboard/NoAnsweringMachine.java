package switchboard;

import java.util.Optional;

public final class NoAnsweringMachine implements AnsweringMachine {

    @Override
    public <T, A> void record(final Deliverable<T, A> deliverable) {
        // nothing to do
    }

    @Override
    public <T, A> Optional<Deliverable<T, A>> removeIf(final Key<T, A> key) {
        return Optional.empty();
    }
}
