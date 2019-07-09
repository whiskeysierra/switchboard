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
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.SubscriptionMode.times;

final class SubscriptionModeTest {

    @TestFactory
    Stream<DynamicTest> createPointTests() {
        return ImmutableMap.<SubscriptionMode, String>builder()
                .put(atLeast(17), "at least 17 times")
                .put(atLeastOnce(), "at least once")
                .put(atMost(17), "at most 17 times")
                .put(exactlyOnce(), "exactly once")
                .put(never(), "not even once")
                .put(times(0), "0 times")
                .put(times(1), "once")
                .put(times(2), "twice")
                .put(times(17), "17 times")
                .build()
                .entrySet().stream()
                .map(entry -> dynamicTest(entry.getValue(), () ->
                        assertThat(entry.getKey(), hasToString(entry.getValue()))));
    }

}
