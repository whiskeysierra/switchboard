package switchboard.contracts;

import switchboard.traits.MessageSubscriptionTrait;
import switchboard.model.Message;

public interface DeliveryContract extends MessageSubscriptionTrait,
        AtLeastContract<Message, String>,
        AtLeastOnceContract<Message, String>,
        AtMostContract<Message, String>,
        AtMostOnceContract<Message, String>,
        ExactlyOnceContract<Message, String>,
        FutureContract<Message, String>,
        NeverContract<Message, String>,
        RecordingContract<Message, String>,
        NoRecordingContract<Message, String>,
        SubscribeContract<Message, String>,
        TimeoutContract<Message, String>,
        TimesContract<Message, String>,
        UnsubscribeContract<Message, String> {
}
