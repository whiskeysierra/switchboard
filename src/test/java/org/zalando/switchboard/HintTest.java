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

import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.zalando.switchboard.Subscription.on;

public final class HintTest {

    private final Switchboard board = Switchboard.create();

    @Test
    public void shouldProvideHint() throws TimeoutException, InterruptedException {
        board.subscribe(on(String.class, "foo"::equals, "bar"));

        assertThat(board.inspect(String.class, String.class), contains("bar"));
    }

}
