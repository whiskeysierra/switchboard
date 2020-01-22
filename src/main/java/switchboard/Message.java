package switchboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
final class Message<T, A> implements Deliverable<T, A> {
    private final Key<T, A> key;
    private final T message;
}
