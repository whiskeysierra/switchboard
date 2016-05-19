package org.zalando.switchboard.contracts;

/*
 * ⁣​
 * Switchboard
 * ⁣⁣
 * Copyright (C) 2015 Zalando SE
 * ⁣⁣
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ​⁣
 */

import org.junit.Assert;
import org.junit.gen5.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.gen5.api.Assertions.expectThrows;
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
        final Switchboard unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialException()));

        final ExecutionException executionException = expectThrows(ExecutionException.class, () -> {
            unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        });

        Assert.assertThat(executionException.getCause(), instanceOf(SpecialException.class));
    }

    @Test
    default void shouldThrowWrappedCheckedException() {
        final Switchboard unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialCheckedException()));

        final ExecutionException executionException = expectThrows(ExecutionException.class, () -> {
            unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        });

        Assert.assertThat(executionException.getCause(), instanceOf(SpecialCheckedException.class));
    }

    @Test
    default void shouldThrowWrappedThrowable() {
        final Switchboard unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialThrowable()));

        final ExecutionException executionException = expectThrows(ExecutionException.class, () -> {
            unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        });

        Assert.assertThat(executionException.getCause(), instanceOf(SpecialThrowable.class));
    }

    @Test
    default void shouldThrowExceptionWithTimeout() {
        final Switchboard unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialException()));

        final ExecutionException exception = expectThrows(ExecutionException.class, () -> {
            unit.subscribe("foo"::equals, atLeastOnce()).get(1, NANOSECONDS);
        });

        assertThat(exception.getCause(), instanceOf(SpecialException.class));
    }

    @Test
    default void shouldThrowExceptionWithoutTimeout() {
        final Switchboard unit = Switchboard.create();

        unit.send(failure("foo", deliveryMode(), new SpecialException()));

        final ExecutionException exception = expectThrows(ExecutionException.class, () -> {
            unit.subscribe("foo"::equals, atLeastOnce()).get();
        });

        assertThat(exception.getCause(), instanceOf(SpecialException.class));
    }

}
