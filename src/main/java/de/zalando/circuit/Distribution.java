package de.zalando.circuit;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public enum Distribution {

    SINGLE {

        @Override
        <E> List<Delivery<E, ?>> distribute(List<Delivery<E, ?>> deliveries) {
            checkState(deliveries.size() == 1, "Too many subcriptions for event, expected one");
            return deliveries;
        }

    },

    FIRST {

        @Override
        <E> List<Delivery<E, ?>> distribute(List<Delivery<E, ?>> deliveries) {
            return deliveries.subList(0, 1);
        }

    },

    BROADCAST {

        @Override
        <E> List<Delivery<E, ?>> distribute(List<Delivery<E, ?>> deliveries) {
            return deliveries;
        }

    };

    // TODO is the order of given subscriptions defined?
    abstract <E> List<Delivery<E, ?>> distribute(List<Delivery<E, ?>> deliveries);

}
