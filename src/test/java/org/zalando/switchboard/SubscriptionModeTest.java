package org.zalando.switchboard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.SubscriptionMode.atLeast;
import static org.zalando.switchboard.SubscriptionMode.atLeastOnce;
import static org.zalando.switchboard.SubscriptionMode.atMost;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.SubscriptionMode.never;
import static org.zalando.switchboard.SubscriptionMode.times;

@RunWith(Parameterized.class)
public final class SubscriptionModeTest {

    private final SubscriptionMode mode;
    private final String expected;

    public SubscriptionModeTest(final SubscriptionMode mode, final String expected) {
        this.mode = mode;
        this.expected = expected;
    }

    @Parameters(name = "{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {atLeast(17), "at least 17"},
                {atLeastOnce(), "at least one"},
                {atMost(17), "at most 17"},
                {exactlyOnce(), "exactly one"},
                {never(), "no"},
                {times(17), "exactly 17"}
        });
    }

    @Test
    public void shouldRenderName() throws Exception {
        assertThat(mode.toString(), is(expected));
    }

}