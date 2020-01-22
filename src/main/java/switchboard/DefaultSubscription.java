package switchboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@AllArgsConstructor(access = PRIVATE)
final class DefaultSubscription<T, A, R> implements Subscription<T, A>, Future<R> {

    private final AtomicReference<State<T, R>> state;

    @Getter
    private final Key<T, A> key;

    DefaultSubscription(
            final Key<T, A> key,
            final Spec<T, R> spec,
            final Consumer<Subscription<T, A>> unregister) {

        this.key = key;
        this.state = new AtomicReference<>(new Waiting<>(spec, () -> unregister.accept(this)));
    }

    @Override
    public boolean deliver(final Deliverable<T, A> deliverable) {
        return state.updateAndGet(current -> current.deliver(deliverable)).isDone();
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return state.updateAndGet(State::cancel).isCancelled();
    }

    @Override
    public boolean isCancelled() {
        return state.get().isCancelled();
    }

    @Override
    public boolean isDone() {
        return state.get().isDone();
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public R get() throws InterruptedException, ExecutionException {
        try {
            return state.updateAndGet(throwingUnaryOperator(State::await)).get();
        } catch (final CancellationException e) {
            throw e;
        } catch (final TimeoutException | RuntimeException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public R get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return state.updateAndGet(throwingUnaryOperator(state -> state.await(timeout, unit))).get();
        } catch (final CancellationException | TimeoutException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new ExecutionException(e);
        }
    }

}
