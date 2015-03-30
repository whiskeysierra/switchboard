package de.zalando.circuit;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class AlwaysUnregisterTest {
    
    private final Circuit circuit = Circuits.create();

    @Test
    public void shouldUnregister() throws TimeoutException {
        // TODO replace with "waitUnless"
        try {
            circuit.receive("foo"::equals, 1, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            // expected
        }
        
        circuit.send("foo", Distribution.SINGLE);
        final String actual = circuit.receive("foo"::equals, 1, TimeUnit.SECONDS);
        assertThat(actual, is("foo"));
    }

}
