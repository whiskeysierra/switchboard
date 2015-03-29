package de.zalando.circuit;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static de.zalando.circuit.Distribution.SINGLE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class LambdaSupportTest {
    
    private final Circuit circuit = Circuits.create();
    
    @Test
    public void shouldSupportLambdas() throws TimeoutException {
        circuit.send("foo", SINGLE);
        assertThat(circuit.receive(this::anyString, 1, SECONDS), is("foo"));
    }

    private boolean anyString(String s) {
        return true;
    }

}
