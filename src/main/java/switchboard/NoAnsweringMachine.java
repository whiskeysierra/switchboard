package switchboard;

import java.util.Collection;
import java.util.Collections;

public final class NoAnsweringMachine implements AnsweringMachine {

    @Override
    public <T, A> void record(final Deliverable<T, A> deliverable) {
        // nothing to do
    }

    @Override
    public <T, A> Collection<Deliverable<T, A>> listen(final Key<T, A> key) {
        return Collections.emptySet();
    }

}
