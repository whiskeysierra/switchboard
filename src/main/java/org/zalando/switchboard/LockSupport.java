package org.zalando.switchboard;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

final class LockSupport {

    private final Lock lock = new ReentrantLock();
    
    void transactional(final Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    <T> T transactional(final Supplier<T> task) {
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

}
