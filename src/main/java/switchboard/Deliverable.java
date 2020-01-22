package switchboard;

public interface Deliverable<T, A> {

    Key<T, A> getKey();
    T getMessage();

    static <T, A> Deliverable<T, A> message(final Key<T, A> key, final T message) {
        return new Message<>(key, message);
    }

}
