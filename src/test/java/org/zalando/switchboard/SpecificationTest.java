package org.zalando.switchboard;

import org.junit.jupiter.api.Disabled;
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

    @Disabled // TODO enable
    @Test
    void shouldExtractTypeFromMethodReference() {
        final Specification<String> unit = "test"::equalsIgnoreCase;
        assertThat(unit.getMessageType(), is(equalTo(String.class)));
    }

    @Test
    void shouldExtractTypeFromMethodReferenceWithExplicitHint() {
        final var unit = on(String.class, "foo"::equals);
        assertThat(unit.getMessageType(), equalTo(String.class));
    }

    @Disabled // TODO enable
    @Test
    void shouldExtractTypeFromLambda() {
        final Specification<String> unit = (String s) -> s.equalsIgnoreCase("test");
        assertThat(unit.getMessageType(), is(equalTo(String.class)));
    }

    @Test
    void shouldSupportLambdas() throws TimeoutException, InterruptedException {
        final Specification<String> specification = (String e) -> true;
        final var actual = unit.subscribe(specification, exactlyOnce());

        // TODO unit.publish(message(123));
        unit.publish(message("foo"));

        assertThat(actual.get(1, NANOSECONDS), is("foo"));
    }

    @Test
    void shouldSupportMethodReference() throws TimeoutException, InterruptedException {
        final Specification<String> spec = "foo"::equals;
        final var actual = unit.subscribe(spec, exactlyOnce());

        // TODO unit.publish(message(123));
        unit.publish(message("foo"));

        assertThat(actual.get(1, NANOSECONDS), is("foo"));
    }

    @Test
    void shouldDelegateToPredicate() throws TimeoutException, InterruptedException {
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
