package org.zalando.switchboard;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class DefaultSwitchboard implements Switchboard {

    private final Registry registry;
    private final AnsweringMachine machine;

    @Override
    public <T, R> Promise<R> subscribe(final Specification<T> specification, final SubscriptionMode<T, R> mode) {
        final var subscription = new DefaultSubscription<>(specification, mode, registry::unregister);

        registry.register(subscription);
        tryToDeliverRecordedMessages(subscription);

        return subscription;
    }

    private <T, R> void tryToDeliverRecordedMessages(final Subscription<T, R> subscription) {
        while (true) {
            final var deliverable = machine.removeIf(subscription);
            deliverable.ifPresent(subscription::deliver);

            if (deliverable.isEmpty() || subscription.isDone()) {
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
