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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.instanceOf;
import static org.zalando.switchboard.SubscriptionMode.exactlyOnce;
import static org.zalando.switchboard.Timeout.in;

public final class ExceptionSupportTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    private final Switchboard unit = Switchboard.create();
    
    private static final class SpecialException extends RuntimeException {
        
    }
    
    @Test
    public void shouldThrowException() throws TimeoutException {
        exception.expect(SpecialException.class);
        
        unit.fail("foo", DeliveryMode.DIRECT, new SpecialException());
        unit.receive("foo"::equals, exactlyOnce(), in(1, TimeUnit.SECONDS));
    }
    
    @Test
    public void shouldThrowExceptionWithTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        exception.expect(ExecutionException.class);
        exception.expectCause(instanceOf(SpecialException.class));

        unit.fail("foo", DeliveryMode.DIRECT, new SpecialException());
        unit.subscribe("foo"::equals, exactlyOnce()).get(1, TimeUnit.SECONDS);
    }
    
    @Test
    public void shouldThrowExceptionWithoutTimeout() throws ExecutionException, InterruptedException {
        exception.expect(ExecutionException.class);
        exception.expectCause(instanceOf(SpecialException.class));
        
        unit.fail("foo", DeliveryMode.DIRECT, new SpecialException());
        unit.subscribe("foo"::equals, exactlyOnce()).get();
    }

}
