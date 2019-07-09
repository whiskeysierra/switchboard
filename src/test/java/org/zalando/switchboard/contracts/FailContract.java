package org.zalando.switchboard.contracts;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.failure;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.within;

public interface FailContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    final class SpecialException extends RuntimeException {

    }

    final class SpecialCheckedException extends Exception {

    }

    final class SpecialThrowable extends Throwable {

    }

    @Test
    default void shouldThrowWrappedException() {
        final var unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialException()));

        final var executionException = assertThrows(ExecutionException.class, () ->
                unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS)));

        Assert.assertThat(executionException.getCause(), instanceOf(SpecialException.class));
    }

    @Test
    default void shouldThrowWrappedCheckedException() {
        final var unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialCheckedException()));

        final var executionException = assertThrows(ExecutionException.class, () ->
                unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS)));

        Assert.assertThat(executionException.getCause(), instanceOf(SpecialCheckedException.class));
    }

    @Test
    default void shouldThrowWrappedThrowable() {
        final var unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialThrowable()));

        final var executionException = assertThrows(ExecutionException.class, () ->
                unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS)));

        Assert.assertThat(executionException.getCause(), instanceOf(SpecialThrowable.class));
    }

    @Test
    default void shouldThrowExceptionWithTimeout() {
        final var unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialException()));

        final var exception = assertThrows(ExecutionException.class, () ->
                unit.subscribe("foo"::equals, atLeastOnce()).get(1, NANOSECONDS));

        assertThat(exception.getCause(), instanceOf(SpecialException.class));
    }

    @Test
    default void shouldThrowExceptionWithoutTimeout() {
        final var unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialException()));

        final var exception = assertThrows(ExecutionException.class, () ->
                unit.subscribe("foo"::equals, atLeastOnce()).get());

        assertThat(exception.getCause(), instanceOf(SpecialException.class));
    }

}
