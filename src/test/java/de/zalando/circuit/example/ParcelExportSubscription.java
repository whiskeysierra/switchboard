package de.zalando.circuit.example;

import de.zalando.circuit.Subscription;

import javax.annotation.Nonnull;
import java.util.Optional;

final class ParcelExportSubscription implements Subscription<ParcelExport,String> {

    private final String parcelId;

    public ParcelExportSubscription(String parcelId) {
        this.parcelId = parcelId;
    }

    @Override
    public boolean test(@Nonnull ParcelExport export) {
        return export.getParcelId().equals(parcelId);
    }

    @Override
    public Optional<String> getHint() {
        return Optional.of(parcelId);
    }

}
