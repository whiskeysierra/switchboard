package org.zalando.switchboard;

import org.junit.jupiter.api.Test;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.Specification.on;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

final class SpecificationTest {

    private final Switchboard unit = Switchboard.create();

    @Test
    void shouldExtractTypeFromGenericTypeArgument() {
        assertThat(new GenericallyTypedSpecification().getMessageType(), is(equalTo(String.class)));
    }

    @Test
    void shouldExtractObjectFromRawTypeArgument() {
        assertThat(new RawTypedSpecification().getMessageType(), is(equalTo(Object.class)));
    }

    @Test
    void shouldSupportLambdas() throws TimeoutException, InterruptedException, ExecutionException {
        unit.publish(message("foo"));
        final Specification<String> specification = (String e) -> true;


        final var actual = unit.subscribe(specification, exactlyOnce()).get(1, NANOSECONDS);
        assertThat(actual, is("foo"));
    }

    @Test
    void shouldSupportMethodReference() throws TimeoutException, InterruptedException, ExecutionException {
        unit.publish(message("foo"));


        final String actual = unit.subscribe("foo"::equals, SubscriptionMode.<String>exactlyOnce()).get(1, NANOSECONDS);
        assertThat(actual, is("foo"));
    }

    @Test
    void shouldProvideMessageType() {
        final var subscription = on(String.class, "foo"::equals);

        assertThat(subscription.getMessageType(), equalTo(String.class));
    }

    @Test
    void shouldDelegateToPredicate() throws TimeoutException, InterruptedException, ExecutionException {
        final var subscription = on(String.class, "foo"::equals);

        unit.publish(message("foo"));


        final var s = unit.subscribe(subscription, atLeastOnce()).get(1, NANOSECONDS);

        assertThat(s, is("foo"));
    }

    @Test
    void shouldNotMatchDifferentType() {
        unit.publish(message(123));

        assertThrows(TimeoutException.class,
                () -> unit.subscribe(on(BigDecimal.class, Number.class::isInstance), atLeastOnce()).get(1, NANOSECONDS));
    }

    @Immutable
    static final class RawTypedSpecification implements Specification {

        @Override
        public boolean test(final Object message) {
            return false;
        }

    }

    @Immutable
    static final class GenericallyTypedSpecification implements Specification<String> {

        @Override
        public boolean test(final String message) {
            return true;
        }

    }
}
