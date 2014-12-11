package de.zalando.circuit.example;

import de.zalando.circuit.Circuit;
import de.zalando.circuit.Circuits;
import de.zalando.circuit.Subscription;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Example {
    
    private final Circuit circuit = Circuits.create();
    
    @Test
    public void test() throws TimeoutException {
        // start process that triggers export in the end...
        
        final ParcelExport export = circuit.receive(exportedParcel("738235167"), 5, MINUTES);

        assertThat(export.getPriority(), is("HIGH"));
    }

    private Subscription<ParcelExport, String> exportedParcel(final String address) {
        return new ParcelExportSubscription(address);
    }

}
