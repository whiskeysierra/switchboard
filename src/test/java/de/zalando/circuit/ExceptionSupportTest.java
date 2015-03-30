package de.zalando.circuit;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ExceptionSupportTest {
    
    private final Circuit circuit = Circuits.create();
    
    private static final class SpecialException extends RuntimeException {
        
    }
    
    @Test(expected = SpecialException.class)
    public void shouldThrowException() throws TimeoutException {
        circuit.fail("foo", DeliveryMode.SINGLE, new SpecialException());
        circuit.receive("foo"::equals, 1, TimeUnit.SECONDS);
    }

}
