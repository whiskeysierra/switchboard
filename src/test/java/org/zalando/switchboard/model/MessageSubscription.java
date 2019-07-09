package org.zalando.switchboard.model;

import org.zalando.switchboard.Subscription;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class MessageSubscription implements Subscription<Message> {

    private final String identifier;

    public MessageSubscription(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean test(final Message input) {
        return identifier.equals(input.getIdentifier());
    }

}
