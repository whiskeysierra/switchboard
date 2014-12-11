package de.zalando.circuit.example;

import de.zalando.circuit.Circuit;
import de.zalando.circuit.Distribution;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.zalando.circuit.Distribution.DIRECT;

final class ParcelExportWorker implements Runnable {

    private final Circuit circuit;

    ParcelExportWorker(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void run() {
        final List<String> parcelIds = circuit.inspect(ParcelExport.class, String.class);
        final List<Map<String, String>> exports = findExportsByParcelId(parcelIds);

        for (Map<String, String> export : exports) {
            circuit.send(new ParcelExport(export.get("address"), export.get("priority")), DIRECT);
        }
    }

    private List<Map<String, String>> findExportsByParcelId(List<String> parcelIds) {
        // obiously fake
        return Collections.emptyList();
    }

}
