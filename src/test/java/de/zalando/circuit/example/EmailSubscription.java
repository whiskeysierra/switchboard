package de.zalando.circuit.example;

import de.zalando.circuit.BaseSubscription;

import javax.annotation.Nullable;

final class EmailSubscription extends BaseSubscription<Email, String> {

    private final String address;

    public EmailSubscription(String address) {
        this.address = address;
    }

    @Override
    public boolean apply(Email email) {
        return email.getSubject().contains("Urgent") &&
                email.getAddress().equals(address);
    }

    @Override
    public String getHint() {
        return address;
    }

}
