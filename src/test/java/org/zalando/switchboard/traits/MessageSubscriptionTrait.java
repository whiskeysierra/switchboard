package org.zalando.switchboard.traits;

import org.zalando.switchboard.Subscription;
import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.model.MessageSubscription;

public interface MessageSubscriptionTrait extends SubscriptionTrait<Message> {

    @Override
    default Subscription<Message> matchA() {
        return new MessageSubscription("A");
    }

    @Override
    default Subscription<Message> matchB() {
        return new MessageSubscription("B");
    }

    @Override
    default Message messageA() {
        return new Message("A");
    }

}
