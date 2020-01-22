package switchboard;

public interface Subscription<T, A> {

    Key<T, A> getKey();
    boolean deliver(Deliverable<T, A> deliverable);

}
