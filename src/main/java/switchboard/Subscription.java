package switchboard;

public interface Subscription<T, A> {

    Key<T, A> getKey();
    void deliver(Deliverable<T, A> deliverable);

}
