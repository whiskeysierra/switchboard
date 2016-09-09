package org.zalando.switchboard;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.Future;

import static java.util.Objects.hash;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.zalando.switchboard.SubscriptionMode.times;

@RunWith(JUnitPlatform.class)
public final class AnswerTest {
    
    private final Switchboard unit = Switchboard.create();
    
    @Test
    public void shouldCreateHashCode() {
        final Subscription<String, Object> subscription = "foo"::equals;
        final Future<List<String>> future = unit.subscribe(subscription, times(1));
        
        assertThat(future.hashCode(), is(hash(subscription)));
    }
    
    @Test
    public void shouldNotBeEqualToDifferentType() {
        final Subscription<String, Object> subscription = "foo"::equals;
        final Future<List<String>> future = unit.subscribe(subscription, times(1));
        
        assertThat(future, is(not(subscription)));
    }

}