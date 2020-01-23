package switchboard;

import com.google.common.util.concurrent.Striped;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

@SuppressWarnings("UnstableApiUsage")
@AllArgsConstructor
final class DefaultSwitchboard implements Switchboard {

    private final Registry registry;
    private final AnsweringMachine machine;
    private final Striped<Lock> striped = Striped.lazyWeakLock(64);

    @Override
    public <T, A, R> Future<R> subscribe(
            final Key<T, A> key,
            final SubscriptionMode<T, R> mode,
            final Duration timeout) {

        final var subscription = new DefaultSubscription<>(
                key, mode, new Timeout(timeout), registry::unregister);

        registry.register(subscription);
        deliveryTo(subscription);

        return subscription;
    }

    private <T, A> void deliveryTo(final Subscription<T, A> subscription) {
        final var key = subscription.getKey();
        final var locking = locking(key);

        locking.run(() -> machine.listen(key)
                .forEach(subscription::deliver));
    }

    @Override
    public <T, A> void publish(final Deliverable<T, A> deliverable) {
        machine.record(deliverable);

        final Key<T, A> key = deliverable.getKey();
        final var locking = locking(key);

        locking.run(() -> registry.find(key)
                .forEach(subscription -> subscription.deliver(deliverable)));
    }

    private <T, A> Locking locking(final Key<T, A> key) {
        return Locking.of(striped.get(key));
    }

}
