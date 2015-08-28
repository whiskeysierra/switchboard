package org.zalando.switchboard;

/*
 * ⁣​
 * Switchboard
 * ⁣⁣
 * Copyright (C) 2015 Zalando SE
 * ⁣⁣
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ​⁣
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
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