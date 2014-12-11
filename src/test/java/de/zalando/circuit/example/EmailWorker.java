package de.zalando.circuit.example;

import de.zalando.circuit.Circuit;
import de.zalando.circuit.Distribution;

import java.util.Collections;
import java.util.List;
import java.util.Map;

final class EmailWorker implements Runnable {

    private final Circuit circuit;

    EmailWorker(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void run() {
        final List<String> addresses = circuit.inspect(Email.class, String.class);
        final List<Map<String, String>> messages = findMessagesBy(addresses);

        for (Map<String, String> message : messages) {
            final Email email = new Email(message.get("address"), message.get("subject"));
            circuit.send(email, Distribution.DIRECT);
        }
    }

    private List<Map<String, String>> findMessagesBy(List<String> addresses) {
        return Collections.emptyList();
    }

}
