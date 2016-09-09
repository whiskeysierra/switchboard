package org.zalando.switchboard.contracts;

import org.junit.jupiter.api.Test;
import org.zalando.switchboard.Switchboard;
import org.zalando.switchboard.model.Message;
import org.zalando.switchboard.traits.DeliveryTrait;
import org.zalando.switchboard.traits.SubscriptionTrait;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.Subscription.on;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;

public interface InspectContract<S> extends SubscriptionTrait<S>, DeliveryTrait {

    @Test // TODO (timeout = TestTimeout.DEFAULT)
    default void shouldAllowToInspectPendingHints() {
        final Switchboard unit = Switchboard.create();

        unit.subscribe(matchA(), exactlyOnce());
        assertThat(unit.inspect(Message.class, String.class), is(singletonList("A")));

        unit.subscribe(matchA(), exactlyOnce());
        assertThat(unit.inspect(Message.class, String.class), is(asList("A", "A")));

        unit.subscribe(matchB(), exactlyOnce());
        assertThat(unit.inspect(Message.class, String.class), containsInAnyOrder(asList("A", "A", "B").toArray()));
    }

    @Test
    default void shouldProvideHint() {
        final Switchboard unit = Switchboard.create();

        unit.subscribe(on(String.class, "foo"::equals, "bar"), exactlyOnce());

        assertThat(unit.inspect(String.class, String.class), contains("bar"));
    }

}
