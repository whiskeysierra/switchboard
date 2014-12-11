package de.zalando.circuit;

import javax.annotation.concurrent.Immutable;

@Immutable
class EventSubscription extends BaseSubscription<Event, String> {

    private final String identifier;

    EventSubscription(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getHint() {
        return identifier;
    }

    @Override
    public boolean apply(final Event input) {
        return identifier.equals(input.getIdentifier());
    }

}
