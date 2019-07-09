package org.zalando.switchboard;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

final class Direct implements DeliveryMode {

    static final DeliveryMode INSTANCE = new Direct();

    private Direct() {
        // singleton
    }

    @Override
    public <S, T> List<Subscription<S, T>> distribute(final List<Subscription<S, T>> deliveries) {
        checkState(deliveries.size() == 1,
                "Too many subscriptions for message %s, expected one",
                deliveries.get(0).getMessageType().getSimpleName());
        return deliveries;
    }

    @Override
    public String toString() {
        return "directly()";
    }

}
