package de.zalando.circuit;

import org.junit.Test;

import java.lang.reflect.Type;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class TypedSubscriptionTest {
    
    private static final class GenericlyTypedSubscription extends TypedSubscription<String, Void> {

        @Override
        public boolean apply(String message) {
            return true;
        }

        @Override
        public Void getMetadata() {
            return null;
        }

    }

    private static class RawTypedSubscription extends TypedSubscription {
        
        @Override
        public boolean apply(Object message) {
            return false;
        }

        @Override
        public Object getMetadata() {
            return null;
        }

    }

    @Test
    public void shouldExtractTypeFromGenericTypeArgument() {
        assertThat(new GenericlyTypedSubscription().getType(), is((Type) String.class));
    }

    @Test(expected = IllegalStateException.class)
    public void unspecificTypeWillFail() {
        new RawTypedSubscription().getType();
    }
    
}
