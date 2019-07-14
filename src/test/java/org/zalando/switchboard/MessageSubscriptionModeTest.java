package org.zalando.switchboard;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.zalando.switchboard.SubscriptionMode.atLeast;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.atMost;
import static org.zalando.switchboard.SubscriptionMode.atMostOnce;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.SubscriptionMode.times;

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
