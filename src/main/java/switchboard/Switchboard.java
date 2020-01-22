package switchboard;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.Future;

/**
 * publish/subscribe, async, hand-over/broadcast
 * deliver previously received messages
 */
@ThreadSafe
public interface Switchboard {

    <T, A, R> Future<R> subscribe(
            Key<T, A> key,
            SubscriptionMode<T, R> mode,
            Duration timeout);

    <T, A> void publish(Deliverable<T, A> deliverable);

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
