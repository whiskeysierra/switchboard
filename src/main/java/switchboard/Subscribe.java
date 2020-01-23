package switchboard;

import java.time.Duration;
import java.util.concurrent.Future;

public interface Subscribe {

    <T, A, R> Future<R> subscribe(
            Key<T, A> key,
            SubscriptionMode<T, R> mode,
            Duration timeout);

}
