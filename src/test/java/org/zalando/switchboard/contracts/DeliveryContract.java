package org.zalando.switchboard.contracts;

import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.traits.MessageSubscriptionTrait;

public interface DeliveryContract extends MessageSubscriptionTrait,
        AtLeastContract<Message>,
        AtLeastOnceContract<Message>,
        AtMostContract<Message>,
        ExactlyOnceContract<Message>,
        FutureContract<Message>,
        NeverContract<Message>,
        RecordingContract<Message>,
        NoRecordingContract<Message>,
        SubscribeContract<Message>,
        TimeoutContract<Message>,
        TimesContract<Message>,
        UnsubscribeContract<Message> {
}
