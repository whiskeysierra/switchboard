package org.zalando.switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received messages
 */
public interface Switchboard {

    <T> void send(Deliverable<T> deliverable);

    <T, R> R receive(Specification<T> specification, SubscriptionMode<T, R> mode, Duration timeout)
            throws ExecutionException, TimeoutException, InterruptedException;

    <T, R> Future<R> subscribe(Specification<T> specification, SubscriptionMode<T, R> mode);

    static Switchboard create() {
        return builder().build();
    }

    static RecipientsStage builder() {
        return new DefaultSwitchboardBuilder();
    }

    interface RecipientsStage extends AnsweringMachineStage {
        AnsweringMachineStage recipients(Subscriptions subscriptions);
    }

    interface AnsweringMachineStage extends BuildStage {
        BuildStage answeringMachine(AnsweringMachine machine);
    }

    interface BuildStage {
        Switchboard build();
    }

}
