package switchboard.contracts;

import org.junit.jupiter.api.Test;
import switchboard.DefaultRegistry;
import switchboard.Deliverable;
import switchboard.NoAnsweringMachine;
import switchboard.SubscriptionMode;
import switchboard.traits.SubscriptionTrait;
import switchboard.Switchboard;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

interface NoRecordingContract<S, A> extends SubscriptionTrait<S, A> {

    @Test
    default void shouldNotRecordMessages() {
        final var unit = Switchboard.builder()
                .registry(new DefaultRegistry()) // to fake coverage
                .answeringMachine(new NoAnsweringMachine())
                .build();

        unit.publish(Deliverable.message(matchA(), messageA()));

        final var exception = assertThrows(ExecutionException.class,
                () -> unit.subscribe(matchA(), SubscriptionMode.atLeastOnce(), Duration.ofMillis(50)).get());

        assertThat(exception.getCause(), is(instanceOf(TimeoutException.class)));
    }

}
