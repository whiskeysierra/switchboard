package org.zalando.switchboard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.switchboard.DeliveryMode.broadcast;
import static org.zalando.switchboard.DeliveryMode.directly;
import static org.zalando.switchboard.DeliveryMode.first;

@RunWith(Parameterized.class)
public final class DeliveryModeTest {

    private final DeliveryMode mode;
    private final String expected;

    public DeliveryModeTest(final DeliveryMode mode, final String expected) {
        this.mode = mode;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {directly(), "directly()"},
                {broadcast(), "broadcast()"},
                {first(), "first()"},
        });
    }

    @Test
    public void shouldRenderName() throws Exception {
        assertThat(mode.toString(), is(expected));
    }

}