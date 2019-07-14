package org.zalando.switchboard;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
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
    void shouldSupportLambdas() throws ExecutionException, InterruptedException {
        final Specification<String> specification = (String e) -> true;
        final var actual = unit.subscribe(specification, exactlyOnce(), Duration.ofMillis(50));

        // TODO unit.publish(message(123));
        unit.publish(message("foo"));

        assertThat(actual.get(), is("foo"));
    }

    @Test
    void shouldSupportMethodReference() throws ExecutionException, InterruptedException {
        final Specification<String> spec = "foo"::equals;
        final var actual = unit.subscribe(spec, exactlyOnce(), Duration.ofMillis(50));

        // TODO unit.publish(message(123));
        unit.publish(message("foo"));

        assertThat(actual.get(), is("foo"));
    }

    @Test
    void shouldDelegateToPredicate() throws ExecutionException, InterruptedException {
        final var subscription = on(String.class, "foo"::equals);

        unit.publish(message("foo"));

        final var s = unit.subscribe(subscription, atLeastOnce(), Duration.ofMillis(50)).get();

        assertThat(s, is("foo"));
    }

    @Test
    void shouldNotMatchDifferentType() {
        unit.publish(message(123));

        final Exception exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(on(BigDecimal.class, Number.class::isInstance), atLeastOnce(),
                                Duration.ofMillis(50)).get());

        final Throwable cause = exception.getCause();
        assertThat(cause, is(instanceOf(TimeoutException.class)));
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
