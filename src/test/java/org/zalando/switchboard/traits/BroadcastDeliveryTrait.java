package org.zalando.switchboard.traits;

import org.zalando.switchboard.DeliveryMode;

public interface BroadcastDeliveryTrait extends DeliveryTrait {

    @Override
    default DeliveryMode deliveryMode() {
        return DeliveryMode.broadcast();
    }

}
