package org.zalando.switchboard.traits;

import org.zalando.switchboard.Specification;
import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.model.MessageSpecification;

public interface MessageSubscriptionTrait extends SubscriptionTrait<Message> {

    @Override
    default Specification<Message> matchA() {
        return new MessageSpecification("A");
    }

    @Override
    default Specification<Message> matchB() {
        return new MessageSpecification("B");
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
