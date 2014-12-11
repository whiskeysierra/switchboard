package de.zalando.circuit;

public final class Circuits {

    private Circuits() {
        // static factory
    }

    public static Circuit create() {
        return new DefaultCircuit();
    }

}
