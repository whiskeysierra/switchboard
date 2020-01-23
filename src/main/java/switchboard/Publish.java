package switchboard;

public interface Publish {
    <T, A> void publish(Deliverable<T, A> deliverable);
}
