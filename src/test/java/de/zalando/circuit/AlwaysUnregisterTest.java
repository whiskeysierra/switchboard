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
        circuit.unless("foo"::equals, 1, TimeUnit.NANOSECONDS);
        circuit.send("foo", DeliveryMode.SINGLE);
        final String actual = circuit.receive("foo"::equals, 1, TimeUnit.SECONDS);
        assertThat(actual, is("foo"));
    }

}
