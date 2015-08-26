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

import static org.zalando.switchboard.DeliveryMode.SINGLE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class LambdaSupportTest {
    
    private final Switchboard board = Switchboard.create();
    
    @Test
    public void shouldSupportLambdas() throws TimeoutException {
        board.send("foo", SINGLE);
        final String actual = board.receive((String e) -> true, 1, SECONDS);
        assertThat(actual, is("foo"));
    }
    
    @Test
    public void shouldSupporMethodReference() throws TimeoutException {
        board.send("foo", SINGLE);
        final String actual = board.receive(this::anyString, 1, SECONDS);
        assertThat(actual, is("foo"));
    }
    
    @Test
    public void shouldSupportInstanceMethodReference() throws TimeoutException {
        board.send("foo", SINGLE);
        final String actual = board.receive("foo"::equals, 1, SECONDS);
        assertThat(actual, is("foo"));
    }

    private boolean anyString(String s) {
        return true;
    }

}
