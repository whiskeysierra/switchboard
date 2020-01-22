package switchboard;

import org.junit.jupiter.api.Test;
import switchboard.contracts.DeliveryContract;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static switchboard.Deliverable.message;
import static switchboard.SubscriptionMode.atLeastOnce;

final class DeliveryTest implements DeliveryContract {

    private final Switchboard unit = Switchboard.create();

    @Test
    void shouldDeliverFirstMessageToAllSubscriptions() {
        assertTimeoutPreemptively(TestTimeout.DEFAULT, () -> {
            final var firstResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));
            final var secondResult = unit.subscribe(matchA(), atLeastOnce(), Duration.ofMillis(50));

            unit.publish(message(matchA(), messageA()));
            unit.publish(message(matchA(), messageA()));

            final var first = firstResult.get();
            final var second = secondResult.get();

            assertThat(first, is(messageA()));
            assertThat(first, is(sameInstance(second)));
        });
    }

}
