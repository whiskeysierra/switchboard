package switchboard.eventbus;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import switchboard.model.Message;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
final class MessageSubscription implements Future<Message> {

    private final String identifier;
    private final EventBus bus;

    private final CompletableFuture<Message> future = new CompletableFuture<Message>()
            .orTimeout(25, MILLISECONDS);

    @Subscribe
    public void onMessage(final Message message) {
        if (message.getIdentifier().equals(identifier)) {
            bus.unregister(this);
            future.complete(message);
        }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        bus.unregister(this);
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public Message get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public Message get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

}
