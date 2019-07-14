package org.zalando.switchboard.eventbus;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.zalando.switchboard.model.Message;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("UnstableApiUsage")
final class EventBusAlternativeTest {

    @Test
    void shouldDeliver() throws ExecutionException, InterruptedException {
        final var bus = new EventBus();

        final var subscription = new MessageSubscription("bob", bus);
        bus.register(subscription);

        bus.post(new Message("alice"));
        bus.post(new Message("bob"));

        final var message = subscription.get();

        assertThat(message.getIdentifier(), is("bob"));
    }

    @Test
    void shouldUnregister() throws InterruptedException {
        final var bus = new EventBus();

        final var subscription = new MessageSubscription("bob", bus);
        bus.register(subscription);

        Thread.sleep(50);

        bus.post(new Message("bob"));

        final var exception = assertThrows(ExecutionException.class, subscription::get);

        assertThat(exception.getCause(), is(instanceOf(TimeoutException.class)));
    }

    @Test
    void shouldTimeout() {
        final var bus = new EventBus();

        final var subscription = new MessageSubscription("bob", bus);
        bus.register(subscription);

        final var exception = assertThrows(ExecutionException.class, subscription::get);

        assertThat(exception.getCause(), is(instanceOf(TimeoutException.class)));
    }

}
