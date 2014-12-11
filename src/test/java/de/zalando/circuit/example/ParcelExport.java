package de.zalando.circuit.example;

public class ParcelExport {

    private final String parcelId;
    private final String priority;

    public ParcelExport(String parcelId, String priority) {
        this.parcelId = parcelId;
        this.priority = priority;
    }

    public String getParcelId() {
        return parcelId;
    }

    public String getPriority() {
        return priority;
    }
    
}
