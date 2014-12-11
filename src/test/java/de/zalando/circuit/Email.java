package de.zalando.circuit;

public class Email {

    private final String address;
    private final String subject;

    public Email(String address, String subject) {
        this.address = address;
        this.subject = subject;
    }

    public String getAddress() {
        return address;
    }

    public String getSubject() {
        return subject;
    }
    
}
