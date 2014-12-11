package de.zalando.circuit;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static de.zalando.circuit.Locking.transactional;
import static java.lang.String.format;

final class Delivery<E, H> implements Future<List<E>>, Predicate<Object> {

    enum State {
        WAITING, DONE, CANCELLED
    }
    
    private final AtomicReference<State> state = new AtomicReference<>(State.WAITING);

    private final Subscription<E, H> subscription;
    private final int count;
    private final Consumer<Delivery<E, H>> deregistration;

    private final BlockingQueue<E> queue;
    private final AtomicInteger pushed = new AtomicInteger();

    private final Lock lock = new ReentrantLock();

    public Delivery(Subscription<E, H> subscription, int count, Consumer<Delivery<E, H>> deregistration) {
        this.subscription = subscription;
        this.count = count;
        this.deregistration = deregistration;
        this.queue = new LinkedBlockingQueue<>(count);
    }

    public Class<E> getEventType() {
        return subscription.getEventType();
    }

    public Class<H> getHintType() {
        return subscription.getHintType();
    }

    public H getMetadata() {
        return subscription.getHint();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean apply(@Nullable Object input) {
        return subscription.getEventType().isInstance(input)
                && subscription.apply((E) input);
    }
    
    void deliver(E event) {
        transactional(lock, () -> {
            queue.add(event);

            final boolean isSatisfied = pushed.incrementAndGet() == count;

            if (isSatisfied) {
                finish(State.DONE);
            }
        });
    }

    private boolean finish(final State endState) {
        deregistration.accept(this);
        return state.compareAndSet(State.WAITING, endState);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        switch (state.get()) {

            case WAITING :
                return finish(State.CANCELLED);

            case DONE :
                return false;

            case CANCELLED :
                return true;

            default :
                throw new AssertionError("Unknown state: " + state);
        }
    }

    @Override
    public boolean isCancelled() {
        return state.get() == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state.get() != State.WAITING;
    }

    @Override
    public List<E> get() throws InterruptedException, ExecutionException {
        try {

            // I consider that "forever"
            return get(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (TimeoutException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public List<E> get(final long timeout, final TimeUnit timeoutUnit) throws InterruptedException, TimeoutException {
        final List<E> results = Lists.newArrayListWithExpectedSize(count);
        final int drained = Queues.drain(queue, results, count, timeout, timeoutUnit);

        if (drained < count) {
            throw failure(timeout, timeoutUnit, drained + 1);
        }

        return results;
    }

    private TimeoutException failure(final long timeout, final TimeUnit timeoutUnit, final int index)
        throws TimeoutException {
        final String typeName = subscription.getEventType().getSimpleName();
        final String timeoutUnitName = timeoutUnit.name().toLowerCase(Locale.ENGLISH);

        final String message;

        if (count == 1) {
            message = format("No [%s] event matching [%s] occurred in [%s] [%s]", typeName, subscription, timeout,
                    timeoutUnitName);
        } else {
            message = format("[%d%s] [%s] event matching [%s] didn't occur in [%s] [%s]", index, ordinal(index),
                    typeName, subscription, timeout, timeoutUnitName);
        }

        throw new TimeoutException(message);
    }

    // TODO move to own class
    private static String ordinal(final int i) {
        switch (i % 100) {

            case 11 :
            case 12 :
            case 13 :
                return "th";

            default :
                switch (i % 10) {

                    case 1 :
                        return "st";

                    case 2 :
                        return "nd";

                    case 3 :
                        return "rd";

                    default :
                        return "th";
                }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription);
    }

    @Override
    public boolean equals(@Nullable final Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof Delivery) {
            final Delivery other = (Delivery) that;
            return Objects.equals(subscription, other.subscription);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return subscription.toString();
    }
    
}
