package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.expectThrows;
import static org.zalando.switchboard.Deliverable.message;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.Timeout.within;

public interface UnsubscribeContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test
    default void shouldUnsubscribe() throws TimeoutException, InterruptedException, ExecutionException {
        final Switchboard unit = Switchboard.create();

        // expected to unsubscribe itself in 1 ns
        unit.receive("foo"::equals, never(), within(1, NANOS));

        unit.send(message("foo", deliveryMode()));
        final String actual = unit.receive("foo"::equals, exactlyOnce(), within(1, NANOS));
        assertThat(actual, is("foo"));
    }

    @Test
    default void cancellingFutureShouldUnsubscribe() throws InterruptedException, ExecutionException, TimeoutException {
        final Switchboard unit = Switchboard.create();

        final Future<S> future = unit.subscribe(matchA(), exactlyOnce());
        future.cancel(false);

        unit.send(message(messageA(), deliveryMode()));

        expectThrows(TimeoutException.class, () -> {
            future.get(1, NANOSECONDS);
        });
    }

}
