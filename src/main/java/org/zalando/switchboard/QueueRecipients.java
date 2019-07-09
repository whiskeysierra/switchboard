package org.zalando.switchboard;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.stream.Collectors.toList;

public final class QueueRecipients implements Recipients {

    private final Queue<Answer<?, ?>> answers = new ConcurrentLinkedQueue<>();

    @Override
    public <T, R> void register(final Answer<T, R> answer) {
        answers.add(answer);
    }

    @Override
    public <T, R> List<Answer<T, R>> find(final T message) {
        return answers.stream()
            .filter(answer -> answer.test(message))
            .map(this::<T, R>cast)
            .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <T, R> Answer<T, R> cast(final Answer answer) {
        return answer;
    }

    @Override
    public <T, R> void unregister(final Answer<T, R> answer) {
        answers.remove(answer);
    }

}
