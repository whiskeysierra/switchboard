package org.zalando.switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.Future;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received messages
 */
@ThreadSafe
public interface Switchboard {

    <T, R> Future<R> subscribe(Specification<T> specification, SubscriptionMode<T, R> mode, Duration timeout);

    <T> void publish(Deliverable<T> deliverable);

    static Switchboard create() {
        return builder().build();
    }

    static RegistryStage builder() {
        return new Builder();
    }

    interface RegistryStage extends AnsweringMachineStage {
        AnsweringMachineStage registry(Registry registry);
    }

    interface AnsweringMachineStage extends BuildStage {
        BuildStage answeringMachine(AnsweringMachine machine);
    }

    interface BuildStage {
        Switchboard build();
    }

}
