package org.zalando.switchboard.traits;

import org.zalando.switchboard.DeliveryMode;

public interface DirectDeliveryTrait extends DeliveryTrait {

    @Override
    default DeliveryMode deliveryMode() {
        return DeliveryMode.directly();
    }

}
