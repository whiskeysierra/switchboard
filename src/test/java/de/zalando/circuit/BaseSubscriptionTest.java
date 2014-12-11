package de.zalando.circuit;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class BaseSubscriptionTest {
    
    private static final class GenericlyTypedSubscription extends BaseSubscription<String, Void> {

        @Override
        public boolean apply(String message) {
            return true;
        }

        @Override
        public Void getHint() {
            return null;
        }

    }

    private static class RawTypedSubscription extends BaseSubscription {
        
        @Override
        public boolean apply(Object message) {
            return false;
        }

        @Override
        public Object getHint() {
            return null;
        }

    }

    @Test
    public void shouldExtractTypeFromGenericTypeArgument() {
        assertThat(new GenericlyTypedSubscription().getEventType(), is(equalTo(String.class)));
        assertThat(new GenericlyTypedSubscription().getHintType(), is(equalTo(Void.class)));
    }

    @Test
    public void unspecificTypeWillFail() {
        assertThat(new RawTypedSubscription().getEventType(), is(equalTo(Object.class)));
        assertThat(new RawTypedSubscription().getHintType(), is(equalTo(Object.class)));
    }
    
}
