package de.zalando.circuit.example;

import de.zalando.circuit.BaseSubscription;

final class ParcelExportSubscription extends BaseSubscription<ParcelExport, String> {

    private final String parcelId;

    public ParcelExportSubscription(String parcelId) {
        this.parcelId = parcelId;
    }

    @Override
    public boolean apply(ParcelExport export) {
        return export.getParcelId().equals(parcelId);
    }

    @Override
    public String getHint() {
        return parcelId;
    }

}
