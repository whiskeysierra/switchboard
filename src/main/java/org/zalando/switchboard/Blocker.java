package org.zalando.switchboard;

import javax.annotation.Nullable;
import java.util.concurrent.BlockingQueue;

interface Blocker<T, X extends Exception> {

    @Nullable
    Deliverable<T> block(final BlockingQueue<Deliverable<T>> queue, int received,
            final SubscriptionMode<T, ?> mode) throws X;

    String format(int received, final SubscriptionMode<T, ?> mode);

}
