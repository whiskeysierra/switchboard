package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.DeliveryMode.broadcast;
import static org.zalando.switchboard.DeliveryMode.directly;
import static org.zalando.switchboard.Subscription.on;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.within;

@RunWith(JUnitPlatform.class)
final class SubscriptionTest {

    private final Switchboard unit = Switchboard.create();

    @Test
    void shouldExtractTypeFromGenericTypeArgument() {
        assertThat(new GenericallyTypedSubscription().getMessageType(), is(equalTo(String.class)));
    }

    @Test
    void shouldExtractObjectFromRawTypeArgument() {
        assertThat(new RawTypedSubscription().getMessageType(), is(equalTo(Object.class)));
    }

    @Test
    void shouldSupportLambdas() throws TimeoutException, InterruptedException, ExecutionException {
        unit.send(message("foo", directly()));
        final Subscription<String, ?> subscription = (String e) -> true;
        final String actual = unit.receive(subscription, exactlyOnce(), within(1, NANOS));
        assertThat(actual, is("foo"));
    }

    @Test
    void shouldSupportMethodReference() throws TimeoutException, InterruptedException, ExecutionException {
        unit.send(message("foo", directly()));
        final String actual = unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        assertThat(actual, is("foo"));
    }

    @Test
    void shouldProvideNoHintByDefault() {
        final Subscription<String, Object> subscription = "foo"::equals;
        assertThat(subscription.getHint(), is(empty()));
    }

    @Test
    void shouldProvideMessageType() {
        final Subscription<String, Object> subscription = on(String.class, "foo"::equals);

        assertThat(subscription.getMessageType(), equalTo(String.class));
    }

    @Test
    void shouldProvideHint() {
        final Subscription<String, String> subscription = on(String.class, "foo"::equals, "bar");
        assertThat(subscription.getHint(), is(Optional.of("bar")));
    }

    @Test
    void shouldDelegateToPredicate() throws TimeoutException, InterruptedException, ExecutionException {
        final Subscription<String, Object> subscription = on(String.class, "foo"::equals);

        unit.send(message("foo", broadcast()));
        final String s = unit.receive(subscription, atLeastOnce(), within(1, NANOS));

        assertThat(s, is("foo"));
    }

    @Test
    void shouldNotMatchDifferentType() {
        unit.send(message(123, broadcast()));

        assertThrows(TimeoutException.class, () ->
                unit.receive(on(BigDecimal.class, Number.class::isInstance), atLeastOnce(), within(1, NANOS)));
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
