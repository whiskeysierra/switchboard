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
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.DeliveryMode.DIRECT;
import static org.zalando.switchboard.Subscription.on;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.in;

public final class SubscriptionTest {

    private final Switchboard unit = Switchboard.create();

    @Test
    public void shouldExtractTypeFromGenericTypeArgument() {
        assertThat(new GenericallyTypedSubscription().getEventType(), is(equalTo(String.class)));
    }

    @Test
    public void shouldExtractObjectFromRawTypeArgument() {
        assertThat(new RawTypedSubscription().getEventType(), is(equalTo(Object.class)));
    }

    @Test
    public void shouldSupportLambdas() throws TimeoutException {
        unit.send("foo", DIRECT);
        final Subscription<String, ?> subscription = (String e) -> true;
        final String actual = unit.receive(subscription, exactlyOnce(), in(1, SECONDS));
        assertThat(actual, is("foo"));
    }

    @Test
    public void shouldSupportMethodReference() throws TimeoutException {
        unit.send("foo", DIRECT);
        final String actual = unit.receive(this::anyString, exactlyOnce(), in(1, SECONDS));
        assertThat(actual, is("foo"));
    }

    @Test
    public void shouldSupportInstanceMethodReference() throws TimeoutException {
        unit.send("foo", DIRECT);
        final String actual = unit.receive("foo"::equals, exactlyOnce(), in(1, SECONDS));
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
    public void shouldProvideEventType() {
        final Subscription<String, Object> subscription = on(String.class, "foo"::equals);

        assertThat(subscription, hasFeature("event type", Subscription::getEventType, equalTo(String.class)));
    }

    @Test
    public void shouldProvideHint() {
        final Subscription<String, String> subscription = on(String.class, "foo"::equals, "bar");
        assertThat(subscription, hasFeature("hint", Subscription::getHint, is(Optional.of("bar"))));
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
