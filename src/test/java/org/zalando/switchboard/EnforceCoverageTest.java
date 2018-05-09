package org.zalando.switchboard;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Hack
@OhNoYouDidnt
@RunWith(JUnitPlatform.class)
final class EnforceCoverageTest {

    @Test
    void shouldVisitDeadCatchClauseInNever() throws ExecutionException, TimeoutException, InterruptedException {
        @SuppressWarnings("unchecked")
        final Future<Void> future = mock(Future.class);
        when(future.get(anyLong(), any())).thenThrow(new TimeoutException());

        final Never<String> mode = new Never<>();
        mode.block(future, 1, TimeUnit.NANOSECONDS);
    }

}
