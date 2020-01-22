package switchboard;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static switchboard.SubscriptionMode.atLeast;
import static switchboard.SubscriptionMode.atLeastOnce;
import static switchboard.SubscriptionMode.atMost;
import static switchboard.SubscriptionMode.atMostOnce;
import static switchboard.SubscriptionMode.exactlyOnce;
import static switchboard.SubscriptionMode.never;
import static switchboard.SubscriptionMode.times;

final class MessageSubscriptionModeTest {

    @TestFactory
    Stream<DynamicTest> createPointTests() {
        return ImmutableMap.<SubscriptionMode, String>builder()
                .put(atLeast(17), "at least 17")
                .put(atLeastOnce(), "at least one")
                .put(atMost(17), "at most 17")
                .put(atMostOnce(), "at most one")
                .put(exactlyOnce(), "exactly one")
                .put(never(), "not even one")
                .put(times(0), "exactly 0")
                .put(times(17), "exactly 17")
                .build()
                .entrySet().stream()
                .map(entry -> dynamicTest(entry.getValue(), () ->
                        assertThat(entry.getKey(), hasToString(entry.getValue()))));
    }

}
