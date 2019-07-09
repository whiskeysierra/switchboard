package org.zalando.switchboard;

import java.util.List;

// TODO don't expose Answer in public interfaces
interface Recipients {

    <T, R> void register(Answer<T, R> answer);

    <T, R> List<Answer<T, R>> find(T message);

    <T, R> void unregister(Answer<T, R> answer);

}
