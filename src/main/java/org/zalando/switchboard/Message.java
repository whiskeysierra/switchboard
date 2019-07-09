package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
final class Message<T> implements Deliverable<T> {

    private final T message;
    private final DeliveryMode deliveryMode;

    @Override
    public void deliverTo(final Collection<? super T> target) {
        target.add(message);
    }

}
