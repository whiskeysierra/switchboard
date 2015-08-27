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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class Errors {

    static String unexpected(final Subscription<?, ?> subscription) {
        return String.format("Didn't expect %s event matching %s", getEventName(subscription), subscription);
    }

    static String unexpected(final Subscription<?, ?> subscription, final long timeout, final TimeUnit timeoutUnit) {
        return String.format("Didn't expect %s event matching %s in %s %s",
                getEventName(subscription), subscription, timeout, humanize(timeoutUnit));
    }

    static String missing(final Subscription<?, ?> subscription, final long timeout, final TimeUnit timeoutUnit) {
        return String.format("Expected %s event matching %s in %s %s",
                getEventName(subscription), subscription, timeout, humanize(timeoutUnit));
    }

    static String timedOut(final Subscription<?, ?> subscription, final int index) {
        if (index == 1) {
            return String.format("No %s event matching %s occurred", getEventName(subscription), subscription);
        } else {
            return String.format("%d%s %s event matching %s didn't occur", index,
                    Ordinals.valueOf(index), getEventName(subscription), subscription);
        }
    }

    static String timedOut(final Subscription<?, ?> subscription, final int index, final long timeout, final TimeUnit timeoutUnit) {
        if (index == 1) {
            return String.format("No %s event matching %s occurred in %s %s", getEventName(subscription), subscription, timeout, humanize(timeoutUnit));
        } else {
            return String.format("%d%s %s event matching %s didn't occur in %s %s", index,
                    Ordinals.valueOf(index), getEventName(subscription), subscription, timeout, humanize(timeoutUnit));
        }
    }

    private static String humanize(final TimeUnit timeoutUnit) {
        return timeoutUnit.name().toLowerCase(Locale.ENGLISH);
    }

    private static String getEventName(final Subscription<?, ?> subscription) {
        return subscription.getEventType().getSimpleName();
    }

}
