package de.zalando.circuit;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
class EventSubscription implements Subscription<Event, String> {

    private final String identifier;

    EventSubscription(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Optional<String> getHint() {
        return Optional.of(identifier);
    }

    @Override
    public boolean test(final Event input) {
        return identifier.equals(input.getIdentifier());
    }

}
