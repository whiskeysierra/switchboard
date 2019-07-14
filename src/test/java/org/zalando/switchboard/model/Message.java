package org.zalando.switchboard.model;

import lombok.Value;

import javax.annotation.concurrent.Immutable;

@Immutable
@Value
public final class Message {

    private final String identifier;

}
