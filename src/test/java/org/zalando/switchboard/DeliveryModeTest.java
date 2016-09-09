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
import static org.zalando.switchboard.DeliveryMode.broadcast;
import static org.zalando.switchboard.DeliveryMode.directly;
import static org.zalando.switchboard.DeliveryMode.first;

@RunWith(JUnitPlatform.class)
public final class DeliveryModeTest {

    @TestFactory
    Stream<DynamicTest> createPointTests() {
        return ImmutableMap.of(
                "directly", directly(),
                "broadcast", broadcast(),
                "first", first())
                .entrySet().stream()
                .map(entry -> dynamicTest(entry.getKey(), () ->
                        assertThat(entry.getValue(), hasToString(entry.getKey() + "()"))));
    }

}