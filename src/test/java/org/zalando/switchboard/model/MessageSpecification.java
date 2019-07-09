package org.zalando.switchboard.model;

import org.zalando.switchboard.Specification;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class MessageSpecification implements Specification<Message> {

    private final String identifier;

    public MessageSpecification(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean test(final Message input) {
        return identifier.equals(input.getIdentifier());
    }

}
