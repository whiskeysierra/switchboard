package switchboard;

import lombok.AllArgsConstructor;
import org.zalando.fauxpas.ThrowingRunnable;
import org.zalando.fauxpas.ThrowingSupplier;

import java.util.concurrent.locks.Lock;

@AllArgsConstructor(staticName = "of")
final class Locking {

    private final Lock lock;

    <X extends Exception> void run(final ThrowingRunnable<X> task) throws X {
        lock.lock();

        try {
            task.tryRun();
        } finally {
            lock.unlock();
        }
    }

    <T, X extends Exception> T run(final ThrowingSupplier<T, X> task) throws X {
        lock.lock();

        try {
            return task.tryGet();
        } finally {
            lock.unlock();
        }
    }

}
