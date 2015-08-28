package org.zalando.switchboard;

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

import org.junit.Test;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.DeliveryMode.broadcast;
import static org.zalando.switchboard.DeliveryMode.directly;
import static org.zalando.switchboard.Subscription.on;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.in;

public final class SubscriptionTest {

    private final Switchboard unit = Switchboard.create();

    @Test
    public void shouldExtractTypeFromGenericTypeArgument() {
        assertThat(new GenericallyTypedSubscription().getMessageType(), is(equalTo(String.class)));
    }

    @Test
    public void shouldExtractObjectFromRawTypeArgument() {
        assertThat(new RawTypedSubscription().getMessageType(), is(equalTo(Object.class)));
    }

    @Test
    public void shouldSupportLambdas() throws TimeoutException, InterruptedException {
        unit.send(message("foo", directly()));
        final Subscription<String, ?> subscription = (String e) -> true;
        final String actual = unit.receive(subscription, exactlyOnce(), in(1, NANOSECONDS));
        assertThat(actual, is("foo"));
    }

    @Test
    public void shouldSupportMethodReference() throws TimeoutException, InterruptedException {
        unit.send(message("foo", directly()));
        final String actual = unit.receive(this::anyString, exactlyOnce(), in(1, NANOSECONDS));
        assertThat(actual, is("foo"));
    }

    @Test
    public void shouldSupportInstanceMethodReference() throws TimeoutException, InterruptedException {
        unit.send(message("foo", directly()));
        final String actual = unit.receive("foo"::equals, exactlyOnce(), in(1, NANOSECONDS));
        assertThat(actual, is("foo"));
    }

    private boolean anyString(final String s) {
        return true;
    }

    @Test
    public void shouldProvideNoHintByDefault() {
        final Subscription<String, Object> subscription = "foo"::equals;
        assertThat(subscription, hasFeature("hint", Subscription::getHint, is(empty())));
    }

    @Test
    public void shouldProvideMessageType() {
        final Subscription<String, Object> subscription = on(String.class, "foo"::equals);

        assertThat(subscription, hasFeature("message type", Subscription::getMessageType, equalTo(String.class)));
    }

    @Test
    public void shouldProvideHint() {
        final Subscription<String, String> subscription = on(String.class, "foo"::equals, "bar");
        assertThat(subscription, hasFeature("hint", Subscription::getHint, is(Optional.of("bar"))));
    }

    @Test
    public void shouldDelegateToPredicate() throws TimeoutException, InterruptedException {
        final Subscription<String, Object> subscription = on(String.class, "foo"::equals);

        unit.send(message("foo", broadcast()));
        final String s = unit.receive(subscription, atLeastOnce(), in(1, NANOSECONDS));

        assertThat(s, is("foo"));
    }

    @Test(expected = TimeoutException.class)
    public void shouldNotMatchDifferentType() throws TimeoutException, InterruptedException {
        unit.send(message(123, broadcast()));
        unit.receive(on(BigDecimal.class, Number.class::isInstance), atLeastOnce(), in(1, NANOSECONDS));
    }

    @Immutable
    static final class RawTypedSubscription implements Subscription {

        @Override
        public boolean test(final Object message) {
            return false;
        }

    }

    @Immutable
    static final class GenericallyTypedSubscription implements Subscription<String, Void> {

        @Override
        public boolean test(final String message) {
            return true;
        }

    }
}
