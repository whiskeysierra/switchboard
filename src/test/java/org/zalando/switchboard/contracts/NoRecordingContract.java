package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.NoAnsweringMachine;
import org.zalando.switchboard.DefaultRegistry;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;

interface NoRecordingContract<S> extends SubscriptionTrait<S> {

    @Test
    default void shouldNotRecordMessages() {
        final var unit = Switchboard.builder()
                .registry(new DefaultRegistry()) // to fake coverage
                .answeringMachine(new NoAnsweringMachine())
                .build();

        unit.publish(message(messageA()));

        final var exception = assertThrows(CompletionException.class, () ->
                unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50)).join());

        assertThat(exception.getCause(), is(instanceOf(TimeoutException.class)));
    }

}
