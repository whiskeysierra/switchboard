package org.zalando.switchboard.model;

import org.zalando.switchboard.Subscription;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
public final class MessageSubscription implements Subscription<Message, String> {

    private final String identifier;

    public MessageSubscription(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Optional<String> getHint() {
        return Optional.of(identifier);
    }

    @Override
    public boolean test(final Message input) {
        return identifier.equals(input.getIdentifier());
    }

}
