package de.zalando.circuit;

import java.util.List;

public interface Distribution {

    <E> List<Subscription<E, ?>> distribute(List<Subscription<E, ?>> subscriptions);

}
