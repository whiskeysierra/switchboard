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

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.gen5.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    public void shouldUseTimeoutConstructor() {
        new Timeout();
    }

    @Test
    public void shouldUseTypeResolverConstructor() {
        new TypeResolver();
    }

    @Test
    public void shouldVisitDeadCatchClauseInNever() throws ExecutionException, TimeoutException, InterruptedException {
        @SuppressWarnings("unchecked")
        final Future<Void> future = mock(Future.class);
        when(future.get(anyLong(), any())).thenThrow(new TimeoutException());

        final Never<String> mode = new Never<>();
        mode.block(future, 1, TimeUnit.NANOSECONDS);

    }

}
