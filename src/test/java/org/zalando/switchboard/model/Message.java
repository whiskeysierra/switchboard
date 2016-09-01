package org.zalando.switchboard.model;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public final class Message {

    private final String identifier;

    public Message(final String identifier) {
        this.identifier = identifier;
    }

    String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof Message) {
            final Message other = (Message) that;
            return Objects.equals(identifier, other.identifier);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

}
