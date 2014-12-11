package de.zalando.circuit;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

final class Locking {

    static void transactional(final Lock lock, Runnable task) {
        transactional(lock, () -> {
            task.run();
            return null;
        });
    }

    static <T> T transactional(final Lock lock, Supplier<T> task) {
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

}
