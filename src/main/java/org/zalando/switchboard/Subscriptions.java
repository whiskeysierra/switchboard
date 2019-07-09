package org.zalando.switchboard;

import java.util.List;

// TODO Directory? PhoneBook? Registry? Register? Index?
interface Subscriptions {

    <T, R> void register(Subscription<T, R> subscription);
    <T, R> List<Subscription<T, R>> find(T message);
    <T, R> void unregister(Subscription<T, R> subscription);

}
