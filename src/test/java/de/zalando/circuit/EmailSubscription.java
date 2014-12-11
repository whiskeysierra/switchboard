package de.zalando.circuit;

final class EmailSubscription extends TypedSubscription<Email, String> {

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
    public String getMetadata() {
        return address;
    }

}
