package org.zalando.switchboard;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

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
        return ImmutableMap.<String, SubscriptionMode>builder()
                .put("at least 17", atLeast(17))
                .put("at least one", atLeastOnce())
                .put("at most 17", atMost(17))
                .put("exactly one", exactlyOnce())
                .put("no", never())
                .put("exactly 17", times(17))
                .build()
                .entrySet().stream()
                .map(entry -> dynamicTest(entry.getKey(), () ->
                        assertThat(entry.getValue(), hasToString(entry.getKey()))));
    }


}
