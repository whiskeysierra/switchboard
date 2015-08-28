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

    private final Queue<Deliverable> recorded = new ConcurrentLinkedQueue<>();
    private final Queue<Answer> answers = new ConcurrentLinkedQueue<>();

    private final LockSupport lock = new LockSupport();

    @Override
    public <T, H> List<H> inspect(final Class<T> messageType, final Class<H> hintType) {
        return copyOf(answers)
                .stream()
                .filter(delivery -> messageType.isAssignableFrom(delivery.getMessageType()))
                .map(delivery -> this.<H>cast(delivery.getHint()))
                .map(hint -> hint.filter(hintType::isInstance).orElse(null))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <H> Optional<H> cast(final Optional hint) {
        return hint;
    }

    private <T, R> List<Answer<T, R, ?>> find(final Deliverable<T> deliverable) {
        return answers.stream()
                .filter(input -> input.test(deliverable.getMessage()))
                .map(this::<T, R>cast)
                .collect(toList());
    }

    @Override
    public <T> void send(final Deliverable<T> deliverable) {
        deliver(deliverable);
    }

    private <T, R> void deliver(final Deliverable<T> deliverable) {
        lock.transactional(() -> {
            final List<Answer<T, R, ?>> matches = find(deliverable);

            if (matches.isEmpty()) {
                recorded.add(deliverable);
            } else {
                final DeliveryMode deliveryMode = deliverable.getDeliveryMode();
                deliverTo(deliveryMode.distribute(matches), deliverable);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T, R> Answer<T, R, ?> cast(final Answer answer) {
        return answer;
    }

    private <T, R> void deliverTo(final List<Answer<T, R, ?>> list, final Deliverable<T> deliverable) {
        for (final Answer<T, R, ?> answer : list) {
            answer.deliver(deliverable);
            LOG.info("Successfully matched message [{}] to [{}]", deliverable.getMessage(), answer);
        }
    }

    <T, R> void unregister(final Answer<T, R, ?> answer) {
        if (answers.remove(answer)) {
            LOG.trace("Unregistered [{}].", answer);
        }
    }

    @Override
    public <T, R, X extends Exception> R receive(final Subscription<T, ?> subscription, final SubscriptionMode<T, R, X> mode, final Timeout timeout)
            throws X, InterruptedException {
        try {
            final Answer<T, R, ?> future = subscribe(subscription, mode);
            return mode.block(future, timeout.getValue(), timeout.getUnit());
        } catch (final ExecutionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    @Override
    public <T, R, X extends Exception> Answer<T, R, ?> subscribe(final Subscription<T, ?> subscription, final SubscriptionMode<T, R, X> mode) {
        final Answer<T, R, ?> answer = new Answer<>(subscription, mode, this::unregister);

        registerForFutureMessages(answer);
        tryDeliverRecordedMessages(answer);

        return answer;
    }

    private <T, R> void registerForFutureMessages(final Answer<T, R, ?> answer) {
        lock.transactional(() -> {
            checkState(!answers.contains(answer), "[%s] is already registered", answer);
            answers.add(answer);
            LOG.trace("Registered [{}]", answer);
        });
    }

    private <T, R> void tryDeliverRecordedMessages(final Answer<T, R, ?> answer) {
        while (!answer.isDone()) {
            final Optional<Deliverable<T>> match = findAndRemove(answer);

            if (match.isPresent()) {
                final Deliverable<T> deliverable = match.get();
                send(deliverable);
                final T message = deliverable.getMessage();
                LOG.info("Successfully matched previously unhandled message [{}] to [{}]", message, answer);
            } else {
                break;
            }
        }
    }

    private <T, R> Optional<Deliverable<T>> findAndRemove(final Answer<T, R, ?> answer) {
        return lock.transactional(() -> {
            final Optional<Deliverable<T>> first = recorded.stream()
                    .filter(deliverable -> answer.test(deliverable.getMessage()))
                    .map(this::<T>cast)
                    .findFirst();

            if (first.isPresent()) {
                recorded.remove(first.get());
            }

            return first;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Deliverable<T> cast(final Deliverable deliverable) {
        return deliverable;
    }

}
