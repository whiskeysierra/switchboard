package de.zalando.circuit;

import javax.annotation.concurrent.Immutable;

@Immutable
class Event {

    private final String identifier;

    Event(final String identifier) {
        this.identifier = identifier;
    }

    String getIdentifier() {
        return identifier;
    }

}
