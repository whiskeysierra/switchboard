package switchboard;

import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.concurrent.Future;

@AllArgsConstructor
final class DefaultSwitchboard implements Switchboard {

    private final Registry registry;
    private final AnsweringMachine machine;

    @Override
    public <T, A, R> Future<R> subscribe(
            final Key<T, A> key,
            final SubscriptionMode<T, R> mode,
            final Duration timeout) {

        final var spec = new Spec<>(mode, timeout);
        final var subscription =
                new DefaultSubscription<>(key, spec, registry::unregister);

        registry.register(key, subscription);
        tryToDeliverRecordedMessages(key, subscription);

        return subscription;
    }

    private <T, A> void tryToDeliverRecordedMessages(
            final Key<T, A> key, final Subscription<T, A> subscription) {

        while (true) {
            final var optional = machine.removeIf(key);

            if (optional.isPresent()) {
                final var deliverable = optional.get();

                // TODO this is needed because the subscription might be unregistered in between and we wouldn't notice
                // TODO find a better way to do this, e.g. re-quering or asking the registry?!
                if (subscription.deliver(deliverable)) {
                    return;
                }
            }

            if (optional.isEmpty()) {
                return;
            }
        }
    }

    @Override
    public <T, A> void publish(final Deliverable<T, A> deliverable) {
        final var matches = registry.find(deliverable);

        if (matches.isEmpty()) {
            machine.record(deliverable);
        } else {
            for (final var subscription : matches) {
                subscription.deliver(deliverable);
            }
        }
    }

}
