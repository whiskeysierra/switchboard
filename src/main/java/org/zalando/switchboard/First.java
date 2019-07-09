package org.zalando.switchboard;

import java.util.List;

final class First implements DeliveryMode {

    static final DeliveryMode INSTANCE = new First();

    private First() {
        // singleton
    }

    @Override
    public <S, T> List<Answer<S, T>> distribute(final List<Answer<S, T>> deliveries) {
        return deliveries.subList(0, 1);
    }

    @Override
    public String toString() {
        return "first()";
    }

}
