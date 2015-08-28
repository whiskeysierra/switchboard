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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.stream.Collectors.toList;

final class DefaultSwitchboard implements Switchboard {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSwitchboard.class);

    private final Queue<Deliverable> pending = new ConcurrentLinkedQueue<>();
    private final Queue<Delivery> deliveries = new ConcurrentLinkedQueue<>();

    private final LockSupport lock = new LockSupport();

    @Override
    public <E, H> List<H> inspect(final Class<E> eventType, final Class<H> hintType) {
        return copyOf(deliveries)
                .stream()
                .filter(delivery -> eventType.isAssignableFrom(delivery.getEventType()))
                .map(delivery -> this.<H>cast(delivery.getHint()))
                .map(hint -> hint.filter(hintType::isInstance).orElse(null))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <H> Optional<H> cast(final Optional hint) {
        return hint;
    }

    private <S, T> List<Delivery<S, T, ?>> find(final Deliverable<S> deliverable) {
        return deliveries.stream()
                .filter(input -> input.test(deliverable.getMessage()))
                .map(this::<S, T>cast)
                .collect(toList());
    }

    @Override
    public <E> void send(final Deliverable<E> deliverable) {
        deliver(deliverable);
    }

    private <S, T> void deliver(final Deliverable<S> deliverable) {
        lock.transactional(() -> {
            final List<Delivery<S, T, ?>> matches = find(deliverable);

            if (matches.isEmpty()) {
                pending.add(deliverable);
            } else {
                final DeliveryMode deliveryMode = deliverable.getDeliveryMode();
                deliverTo(deliveryMode.distribute(matches), deliverable);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <S, T> Delivery<S, T, ?> cast(final Delivery delivery) {
        return delivery;
    }

    private <S, T> void deliverTo(final List<Delivery<S, T, ?>> list, final Deliverable<S> deliverable) {
        for (final Delivery<S, T, ?> delivery : list) {
            delivery.deliver(deliverable);
            LOG.info("Successfully matched event [{}] to [{}]", deliverable.getMessage(), delivery);
        }
    }

    <E, S> void unregister(final Delivery<E, S, ?> delivery) {
        if (deliveries.remove(delivery)) {
            LOG.trace("Unregistered [{}].", delivery);
        }
    }

    @Override
    public <S, T, X extends Exception> T receive(final Subscription<S, ?> subscription, final SubscriptionMode<S, T, X> mode, final Timeout timeout)
            throws X, InterruptedException {
        try {
            final Delivery<S, T, ?> future = subscribe(subscription, mode);
            return mode.block(future, timeout.getValue(), timeout.getUnit());
        } catch (final ExecutionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    @Override
    public <S, T, X extends Exception> Delivery<S, T, ?> subscribe(final Subscription<S, ?> subscription, final SubscriptionMode<S, T, X> mode) {
        final Delivery<S, T, ?> delivery = new Delivery<>(subscription, mode, this::unregister);

        registerForFutureEvents(delivery);
        tryDeliverUnhandledEvents(delivery);

        return delivery;
    }

    private <S, T> void registerForFutureEvents(final Delivery<S, T, ?> subscription) {
        lock.transactional(() -> {
            checkState(!deliveries.contains(subscription), "[%s] is already registered", subscription);
            deliveries.add(subscription);
            LOG.trace("Registered [{}]", subscription);
        });
    }

    private <S, T> void tryDeliverUnhandledEvents(final Delivery<S, T, ?> delivery) {
        while (!delivery.isDone()) {
            final Optional<Deliverable<S>> match = findAndRemove(delivery);

            if (match.isPresent()) {
                final Deliverable<S> deliverable = match.get();
                send(deliverable);
                final S event = deliverable.getMessage();
                LOG.info("Successfully matched previously unhandled event [{}] to [{}]", event, delivery);
            } else {
                break;
            }
        }
    }

    private <S, T> Optional<Deliverable<S>> findAndRemove(final Delivery<S, T, ?> delivery) {
        return lock.transactional(() -> {
            final Optional<Deliverable<S>> first = pending.stream()
                    .filter(event -> delivery.test(event.getMessage()))
                    .map(this::<S>cast)
                    .findFirst();

            if (first.isPresent()) {
                pending.remove(first.get());
            }

            return first;
        });
    }

    @SuppressWarnings("unchecked")
    private <E> Deliverable<E> cast(final Deliverable event) {
        return event;
    }

}
