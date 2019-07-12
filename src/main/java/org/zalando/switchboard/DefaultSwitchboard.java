package org.zalando.switchboard;

import lombok.AllArgsConstructor;

import java.time.Duration;

@AllArgsConstructor
final class DefaultSwitchboard implements Switchboard {

    private final Registry registry;
    private final AnsweringMachine machine;

    @Override
    public <T, R> Promise<R> subscribe(
            final Specification<T> specification,
            final SubscriptionMode<T, R> mode,
            final Duration timeout) {

        final var spec = new Spec<>(specification, mode, timeout);
        final var subscription = new DefaultSubscription<>(spec, registry::unregister);
        // TODO unregister when done!

        registry.register(subscription);
        tryToDeliverRecordedMessages(subscription);

        return subscription;
    }

    private <T> void tryToDeliverRecordedMessages(final Subscription<T> subscription) {
        while (true) {
            final var optional = machine.removeIf(subscription);

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
    public <T> void publish(final Deliverable<T> deliverable) {
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
