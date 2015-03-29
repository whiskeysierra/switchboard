package de.zalando.circuit;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class SubscriptionTest {
    
    private static final class GenericlyTypedSubscription implements Subscription<String, Void> {

        @Override
        public boolean test(String message) {
            return true;
        }

        @Override
        public Optional<Void> getHint() {
            return Optional.empty();
        }

    }

    private static class RawTypedSubscription implements Subscription {
        
        @Override
        public boolean test(Object message) {
            return false;
        }

        @Override
        public Optional<Object> getHint() {
            return Optional.empty();
        }

    }

    @Test
    public void shouldExtractTypeFromGenericTypeArgument() {
        assertThat(new GenericlyTypedSubscription().getEventType(), is(equalTo(String.class)));
    }

    @Test
    public void unspecificTypeWillFail() {
        assertThat(new RawTypedSubscription().getEventType(), is(equalTo(Object.class)));
    }
    
}
