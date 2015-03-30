package de.zalando.circuit;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static de.zalando.circuit.DeliveryMode.SINGLE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class LambdaSupportTest {
    
    private final Circuit circuit = Circuits.create();
    
    @Test
    public void shouldSupportLambdas() throws TimeoutException {
        circuit.send("foo", SINGLE);
        final String actual = circuit.receive((String e) -> true, 1, SECONDS);
        assertThat(actual, is("foo"));
    }
    
    @Test
    public void shouldSupporMethodReference() throws TimeoutException {
        circuit.send("foo", SINGLE);
        final String actual = circuit.receive(this::anyString, 1, SECONDS);
        assertThat(actual, is("foo"));
    }
    
    @Test
    public void shouldSupportInstanceMethodReference() throws TimeoutException {
        circuit.send("foo", SINGLE);
        final String actual = circuit.receive("foo"::equals, 1, SECONDS);
        assertThat(actual, is("foo"));
    }

    private boolean anyString(String s) {
        return true;
    }

}
