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

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

final class Delivery<E, H> implements Future<List<E>>, Predicate<Object> {

    enum State {
        WAITING, DONE, CANCELLED
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.WAITING);

    private final Subscription<E, H> subscription;
    private final int count;
    private final Consumer<Delivery<E, H>> unregister;

    private final BlockingQueue<Deliverable<E>> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger delivered = new AtomicInteger();

    private final LockSupport lock = new LockSupport();

    Delivery(Subscription<E, H> subscription, int count, Consumer<Delivery<E, H>> unregister) {
        this.subscription = subscription;
        this.count = count;
        this.unregister = unregister;
    }

    Class<E> getEventType() {
        return subscription.getEventType();
    }

    Optional<H> getHint() {
        return subscription.getHint();
    }

    @Override
    public boolean test(@Nullable Object input) {
        return subscription.getEventType().isInstance(input) && subscription.test(cast(input));
    }

    @SuppressWarnings("unchecked")
    private E cast(Object input) {
        return (E) input;
    }

    void deliver(Deliverable<E> deliverable) {
        lock.transactional(() -> {
            queue.add(deliverable);

            final boolean isSatisfied = delivered.incrementAndGet() == count;

            if (isSatisfied) {
                finish(State.DONE);
            }
        });
    }

    private boolean finish(final State endState) {
        unregister();
        return state.compareAndSet(State.WAITING, endState);
    }

    private void unregister() {
        unregister.accept(this);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        switch (state.get()) {

            case WAITING:
                return finish(State.CANCELLED);

            case DONE:
                return false;

            default:
                return true;
        }
    }

    @Override
    public boolean isCancelled() {
        return state.get() == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state.get() != State.WAITING;
    }

    @Override
    public List<E> get() throws InterruptedException, ExecutionException {
        try {
            final List<E> results = Lists.newArrayListWithExpectedSize(count);

            int drained = 0;

            while (drained < count) {
                final Deliverable<E> deliverable = queue.take();

                try {
                    deliverable.deliverTo(results);
                } catch (RuntimeException e) {
                    throw new ExecutionException(e);
                }

                drained++;
            }

            return results;
        } finally {
            unregister();
        }
    }

    @Override
    public List<E> get(final long timeout, final TimeUnit timeoutUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        try {
            final List<E> results = Lists.newArrayListWithExpectedSize(count);

            final long deadline = System.nanoTime() + timeoutUnit.toNanos(timeout);
            int drained = 0;

            while (drained < count) {
                final Deliverable<E> deliverable = queue.poll(deadline - System.nanoTime(), NANOSECONDS);
                if (deliverable == null) {
                    break;
                }

                try {
                    deliverable.deliverTo(results);
                } catch (RuntimeException e) {
                    throw new ExecutionException(e);
                }

                drained++;
            }

            if (drained < count) {
                throw failure(timeout, timeoutUnit, drained + 1);
            }

            return results;
        } finally {
            unregister();
        }
    }

    private TimeoutException failure(final long timeout, final TimeUnit timeoutUnit, final int index)
            throws TimeoutException {
        final String typeName = subscription.getEventType().getSimpleName();
        final String timeoutUnitName = timeoutUnit.name().toLowerCase(Locale.ENGLISH);

        final String message;

        if (count == 1) {
            message = format("No [%s] event matching [%s] occurred in [%s] [%s]", typeName, subscription, timeout,
                    timeoutUnitName);
        } else {
            message = format("[%d%s] [%s] event matching [%s] didn't occur in [%s] [%s]", index,
                    Ordinals.valueOf(index), typeName, subscription, timeout, timeoutUnitName);
        }

        throw new TimeoutException(message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription);
    }

    @Override
    public boolean equals(@Nullable final Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof Delivery) {
            final Delivery other = (Delivery) that;
            return Objects.equals(subscription, other.subscription);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return subscription.toString();
    }

}
