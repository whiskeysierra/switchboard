package org.zalando.circuit;

/*
 * ⁣​
 * Circuit
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static java.util.Collections.frequency;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class DefaultCircuitTest {

    private final DeliveryMode deliveryMode;
    private final Event event1 = new Event("A");
    private final Event event2 = new Event("A");
    private final Subscription<Event, String> matcher1 = new EventSubscription("A");
    private final Subscription<Event, String> matcher2 = new EventSubscription("A");
    private final Subscription<Event, String> matcher3 = new EventSubscription("B");

    private final Circuit unit = Circuit.create();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public DefaultCircuitTest(final DeliveryMode deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    //J-
    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {DeliveryMode.SINGLE},
                {DeliveryMode.FIRST},
                {DeliveryMode.BROADCAST}
        });
    }
    //J+

    @Test(timeout = 250)
    public void shouldDeliverUnhandledEventsPairwiseToMatchers() throws TimeoutException, InterruptedException,
            ExecutionException {
        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);

        final Future<Event> firstResult = unit.subscribe(matcher1);
        final Event first = firstResult.get();

        final Future<Event> secondResult = unit.subscribe(matcher2);
        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(second, is(event2));
    }

    @Test(timeout = 250)
    public void shouldDeliverUnhandledEventsPairwiseToConcurrentMatchers() throws TimeoutException,
            InterruptedException, ExecutionException {
        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);

        final Future<Event> firstResult = unit.subscribe(matcher1);
        final Future<Event> secondResult = unit.subscribe(matcher2);

        final Event first = firstResult.get();
        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(second, is(event2));
    }

    @Test(timeout = 250)
    public void shouldDeliverUnhandledEventsPairwiseToMatchersOneAtATime() throws InterruptedException,
            ExecutionException {
        unit.send(event1, deliveryMode);

        final Future<Event> firstResult = unit.subscribe(matcher1);
        final Event first = firstResult.get();

        unit.send(event2, deliveryMode);

        final Future<Event> secondResult = unit.subscribe(matcher2);
        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(second, is(event2));
    }

    @Test(timeout = 250)
    public void shouldDeliverPartlyUnhandledEventsPairwiseToMatchersOneAtATime() throws InterruptedException,
            ExecutionException {
        unit.send(event1, deliveryMode);

        final Future<Event> firstResult = unit.subscribe(matcher1);
        final Event first = firstResult.get();

        final Future<Event> secondResult = unit.subscribe(matcher2);

        unit.send(event2, deliveryMode);

        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(second, is(event2));
    }

    @Test(timeout = 250)
    public void shouldDeliverPartlyUnhandledEventsPairwiseToConcurrentMatchers() throws InterruptedException,
            ExecutionException {
        unit.send(event1, deliveryMode);

        final Future<Event> firstResult = unit.subscribe(matcher1);
        final Future<Event> secondResult = unit.subscribe(matcher2);

        unit.send(event2, deliveryMode);

        final Event first = firstResult.get();
        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(second, is(event2));
    }

    @Test(timeout = 250)
    public void shouldDeliverEventsPairwiseToMatchers() throws InterruptedException, ExecutionException {
        unit.send(event1, deliveryMode);

        final Future<Event> firstResult = unit.subscribe(matcher1);

        unit.send(event2, deliveryMode);

        final Future<Event> secondResult = unit.subscribe(matcher2);

        final Event first = firstResult.get();
        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(second, is(event2));
    }

    @Test(expected = IllegalStateException.class, timeout = 250)
    public void shouldThrowWhenDeliveringEventsToMatchers() {
        assumeThat(deliveryMode, is(DeliveryMode.SINGLE));

        unit.subscribe(matcher1);
        unit.subscribe(matcher2);

        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);
    }

    @Test(timeout = 250)
    public void shouldDeliverEventsToMatchers() throws ExecutionException, InterruptedException {
        assumeThat(deliveryMode, is(DeliveryMode.FIRST));

        final Future<Event> firstResult = unit.subscribe(matcher1);
        final Future<Event> secondResult = unit.subscribe(matcher2);

        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);

        final Event first = firstResult.get();
        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(second, is(event2));
    }

    @Test(timeout = 250)
    public void shouldDeliverFirstEventToAllMatchers() throws ExecutionException, InterruptedException {
        assumeThat(deliveryMode, is(DeliveryMode.BROADCAST));

        final Future<Event> firstResult = unit.subscribe(matcher1);
        final Future<Event> secondResult = unit.subscribe(matcher2);

        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);

        final Event first = firstResult.get();
        final Event second = secondResult.get();

        assertThat(first, is(event1));
        assertThat(first, is(sameInstance(second)));
    }

    @Test(expected = TimeoutException.class, timeout = 250)
    public void shouldTimeoutWhenThereAreNomatchingEvents() throws TimeoutException {
        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);

        unit.receive(matcher3, 10, MILLISECONDS);
    }

    @Test(timeout = 250)
    public void shouldGiveIdentifiersOfMatchers() {
        unit.subscribe(matcher1);
        assertThat(unit.inspect(Event.class, String.class), is(asList("A")));

        unit.subscribe(matcher2);
        assertThat(unit.inspect(Event.class, String.class), is(asList("A", "A")));

        unit.subscribe(matcher3);
        assertThat(unit.inspect(Event.class, String.class), containsInAnyOrder(asList("A", "A", "B").toArray()));
    }

    @Test(timeout = 250)
    public void shouldPollMultipleTimesWhenCountGiven() throws TimeoutException {
        final int count = 5;

        for (int i = 0; i < 5; i++) {
            unit.send(event1, deliveryMode);
        }

        final List<Event> events = unit.receive(matcher1, count, 100, MILLISECONDS);

        assertThat(events, hasSize(count));
        assertThat(frequency(events, event1), is(count));
    }

    @Test(timeout = 250)
    public void shoudPollAsyncMultipleTimesWhenCountGiven() throws ExecutionException, InterruptedException {
        final int count = 5;

        final Future<List<Event>> future = unit.subscribe(matcher1, count);

        for (int i = 0; i < 5; i++) {
            unit.send(event1, deliveryMode);
        }

        final List<Event> events = future.get();

        assertThat(events, hasSize(count));
        assertThat(frequency(events, event1), is(count));
    }

    @Test(timeout = 250)
    public void shoudPollAsyncWithTimeoutMultipleTimesWhenCountGiven() throws ExecutionException, InterruptedException,
            TimeoutException {
        final int count = 5;

        final Future<List<Event>> future = unit.subscribe(matcher1, count);

        for (int i = 0; i < 5; i++) {
            unit.send(event1, deliveryMode);
        }

        final List<Event> events = future.get(50L, MILLISECONDS);

        assertThat(events, hasSize(count));
        assertThat(frequency(events, event1), is(count));
    }

    @Test(timeout = 250)
    public void shouldTellThatSecondEventDidNotOccur() throws TimeoutException {
        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);

        exception.expect(TimeoutException.class);
        exception.expectMessage(containsString("3rd"));

        unit.receive(matcher1, 3, 100L, MILLISECONDS);
    }

    @Test(timeout = 250)
    public void shouldTellThatSecondEventDidNotOccurWhenPollingAsync() throws TimeoutException, ExecutionException,
            InterruptedException {

        unit.send(event1, deliveryMode);
        unit.send(event2, deliveryMode);

        exception.expect(TimeoutException.class);
        exception.expectMessage(containsString("3rd"));

        unit.subscribe(matcher1, 3).get(100, MILLISECONDS);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenRegisteringTwice() {
        unit.subscribe(matcher1);
        unit.subscribe(matcher1);
    }

    @Test(expected = TimeoutException.class)
    public void cancellingAsyncPollShouldUnregisterPredicate() throws InterruptedException, ExecutionException,
            TimeoutException {

        final Future<Event> future = unit.subscribe(matcher1);
        future.cancel(false);

        unit.send(event1, deliveryMode);
        future.get(100, MILLISECONDS);
    }

    @Test
    public void successfulFutureShoudBeDone() {
        final Future<Event> future = unit.subscribe(matcher1);
        unit.send(event1, deliveryMode);

        assertThat(future.isDone(), is(true));
    }

    @Test
    public void successfulFutureShoudNotBeCancelled() {
        final Future<Event> future = unit.subscribe(matcher1);
        unit.send(event1, deliveryMode);

        assertThat(future.isCancelled(), is(false));
    }

    @Test
    public void cancelledFutureShoudBeDone() {
        final Future<Event> future = unit.subscribe(matcher1);
        future.cancel(false);

        assertThat(future.isDone(), is(true));
    }

    @Test
    public void cancelledFutureShoudBeCancelled() {
        final Future<Event> future = unit.subscribe(matcher1);

        future.cancel(false);

        assertThat(future.isCancelled(), is(true));
    }

    @Test
    public void cancellingWaitingFutureShouldSucceed() {
        assertThat(unit.subscribe(matcher1).cancel(false), is(true));
    }

    @Test
    public void cancellingDoneFutureShouldNotSucceed() {
        final Future<Event> future = unit.subscribe(matcher1);
        unit.send(event1, deliveryMode);

        assertThat(future.cancel(true), is(false));
    }
    
    @Test
    public void cancellingCancelledFutureShouldSuceed() {
        final Future<Event> future = unit.subscribe(matcher1);
        future.cancel(false);
        
        assertThat(future.cancel(false), is(true));
    }

}
