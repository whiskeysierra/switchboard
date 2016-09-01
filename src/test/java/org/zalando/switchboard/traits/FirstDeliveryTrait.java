package org.zalando.switchboard.traits;

import org.zalando.switchboard.DeliveryMode;

public interface FirstDeliveryTrait extends DeliveryTrait {

    @Override
    default DeliveryMode deliveryMode() {
        return DeliveryMode.first();
    }

}
