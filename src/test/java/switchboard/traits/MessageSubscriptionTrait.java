package switchboard.traits;

import switchboard.Key;
import switchboard.model.Message;

public interface MessageSubscriptionTrait extends SubscriptionTrait<Message, String> {

    @Override
    default Key<Message, String> matchA() {
        return Key.of(Message.class, "A");
    }

    @Override
    default Key<Message, String> matchB() {
        return Key.of(Message.class, "B");
    }

    @Override
    default Message messageA() {
        return new Message("A");
    }

    @Override
    default Message messageB() {
        return new Message("B");
    }

}
