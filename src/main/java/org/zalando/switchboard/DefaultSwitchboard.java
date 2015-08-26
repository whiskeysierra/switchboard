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
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

final class DefaultSwitchboard implements Switchboard {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSwitchboard.class);

    private final Queue<Deliverable> pending = new ConcurrentLinkedQueue<>();
    private final Queue<Delivery> deliveries = new ConcurrentLinkedQueue<>();

    private final LockSupport lock = new LockSupport();

    @Override
    public <E, H> List<H> inspect(Class<E> eventType, Class<H> hintType) {
        return copyOf(deliveries)
                .stream()
                .filter(delivery -> eventType.isAssignableFrom(delivery.getEventType()))
                .map(delivery -> this.<H>cast(delivery.getHint()))
                .map(hint -> hint.filter(hintType::isInstance).orElse(null))
                .collect(toList());
    }
    
    @SuppressWarnings("unchecked")
    private <H> Optional<H> cast(Optional hint) {
        return hint;
    }

    private <E> List<Delivery<E, ?>> find(Deliverable<E> deliverable) {
        return deliveries.stream()
                .filter(input -> input.test(deliverable.getEvent()))
                .map(this::<E>cast)
                .collect(toList());
    }

    @Override
    public <E> void send(E event, DeliveryMode deliveryMode) {
        deliver(new QueuedEvent<>(event, deliveryMode));
    }

    @Override
    public <E> void fail(E event, DeliveryMode deliveryMode, RuntimeException exception) {
        deliver(new QueuedError<>(event, deliveryMode, exception));
    }

    private <E> void deliver(Deliverable<E> deliverable) {
        lock.transactional(() -> {
            final List<Delivery<E, ?>> matches = find(deliverable);
            
            if (matches.isEmpty()) {
                pending.add(deliverable);
            } else {
                final DeliveryMode deliveryMode = deliverable.getDeliveryMode();
                deliverTo(deliveryMode.distribute(matches), deliverable);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <E> Delivery<E, ?> cast(Delivery delivery) {
        return delivery;
    }

    private <E> void deliverTo(final List<Delivery<E, ?>> list, final Deliverable<E> deliverable) {
        for (Delivery<E, ?> delivery : list) {
            delivery.deliver(deliverable);
            LOG.info("Successfully matched event [{}] to [{}]", deliverable.getEvent(), delivery);
        }
    }

    <E> void unregister(final Delivery<E, ?> delivery) {
        if (deliveries.remove(delivery)) {
            LOG.trace("Unregistered [{}].", delivery);
        }
    }

    @Override
    public <E> Future<List<E>> subscribe(Subscription<E, ?> subscription, int count) {
        final Delivery<E, ?> delivery = new Delivery<>(subscription, count, this::unregister);

        registerForFutureEvents(delivery);
        tryDeliverUnhandledEvents(delivery);

        return delivery;
    }

    private <E> void registerForFutureEvents(final Delivery<E, ?> subscription) {
        lock.transactional(() -> {
            checkState(!deliveries.contains(subscription), "[%s] is already registered", subscription);
            deliveries.add(subscription);
            LOG.trace("Registered [{}]", subscription);
        });
    }

    private <E> void tryDeliverUnhandledEvents(final Delivery<E, ?> delivery) {
        while (!delivery.isDone()) {
            final Optional<Deliverable<E>> match = findAndRemove(delivery);

            if (match.isPresent()) {
                final Deliverable<E> deliverable = match.get();
                deliverable.sendTo(this);
                final E event = deliverable.getEvent();
                LOG.info("Successfully matched previously unhandled event [{}] to [{}]", event, delivery);
            } else {
                break;
            }
        }
    }

    private <E> Optional<Deliverable<E>> findAndRemove(final Delivery<E, ?> delivery) {
        return lock.transactional(() -> {
            final Optional<Deliverable<E>> first = pending.stream()
                    .filter(event -> delivery.test(event.getEvent()))
                    .map(this::<E>cast)
                    .findFirst();

            if (first.isPresent()) {
                pending.remove(first.get());
            }

            return first;
        });
    }
    
    @SuppressWarnings("unchecked")
    private <E> Deliverable<E> cast(Deliverable event) {
        return event;
    }

}
