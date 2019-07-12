package org.zalando.switchboard;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface Promise<V> extends Future<V> {

    default V join() throws CompletionException {
        while (true) {
            try {
                return get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                // TODO recursion vs while?!
            } catch (final ExecutionException e) {
                throw new CompletionException(e.getCause());
                // TODO throw CompletionException as-is?
            } catch (final CancellationException e) {
                throw e;
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        }
    }

}
