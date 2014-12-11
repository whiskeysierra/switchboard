package de.zalando.circuit;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MINUTES;

public class Example {
    
    final Circuit circuit = new DefaultCircuit();
    
    @Test
    public void test() throws TimeoutException {
        // trigger process that trigger email...
        
        final Email email = circuit.receive(emailTo("info@example.com"), 5, MINUTES);
        // use email
    }

    private Subscription<Email, String> emailTo(final String address) {
        return new EmailSubscription(address);
    }

}
