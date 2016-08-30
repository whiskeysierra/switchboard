# Switchboard

[![Switchboard](docs/switchboard.jpg)](https://www.flickr.com/photos/justininsd/7888302222/)

[![Build Status](https://img.shields.io/travis/zalando/switchboard/master.svg)](https://travis-ci.org/zalando/switchboard)
[![Coverage Status](https://img.shields.io/coveralls/zalando/switchboard/master.svg)](https://coveralls.io/r/zalando/switchboard)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.zalando/switchboard/badge.svg)](http://www.javadoc.io/doc/org.zalando/switchboard)
[![Release](https://img.shields.io/github/release/zalando/switchboard.svg)](https://github.com/zalando/switchboard/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/switchboard.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/switchboard)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/zalando-incubator/switchboard/master/LICENSE)

An in-process message router that helps to write simple, asynchronous, state-based and collaboration tests. 
    
## Dependency

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>switchboard</artifactId>
    <version>${switchboard.version}</version>
    <scope>test</scope>
</dependency>
```
    
## Concept

In any non-trivial application you'll most probably seen a process like this.

![Synchronous Interaction](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgU3luY2hyb25vdXMgSW50ZXJhY3Rpb24KCkNsaWVudC0-U2VydmVyOiBSZXF1ZXN0CgAKBi0-RGF0YWJhc2UAEAoACggAKAxzcG9uc2UALAkATwYADgs&s=napkin)
 
Since the call to the database is synchronous we can be sure it happened before the client got the response. Testing this case is therefore relatively 
straightforward:

```java
Response response = server.save(bob);
assertThat(database.getUser("bob"), is(not(nullValue())));
```

It all gets more difficult when you start to do time-consuming tasks in an asynchronous fashion:
 
![Asynchronous Interaction](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgQXN5bmNocm9ub3VzIEludGVyYWN0aW9uCgpDbGllbnQtPlNlcnZlcjogUmVxdWVzdAoACgYtPkRhdGFiYXNlAAgSADQGOiBSZXNwb25zZQoAIwgAQQwAFAc&s=napkin)
 
The test case from above may no longer work, since we may get the response back from the server before the database finished its part.

The idea of *Switchboard* is to encapsulate the necessary event-based communication behind a small API that allows to write synchronous-style assertions for 
asynchronous messages.

The term *switchboard* refers to old-style telephone switchboards, i.e. big communication devices handled by a switchboard operator that allow to connect
multiple parties via telephone lines, usually one caller and one receiver.
 
In our case those parties are threads and they *talk* to each other by passing messages.

## Usage

Any communication via *Switchboard* consists of two parts: [Receiving](#receiving-messages) and [sending messages](#sending-messages).

### Receiving messages

You receive messages by [subscribing](#subscriptions) to it. Subscriptions can be done either in a [blocking](#blocking) or [non-blocking](#non-blocking) 
fashion. Additionally one specifies a [*subscription mode*](#subscription-modes) to indicate how much messages are going to be consumed.

Think of *blocking subscriptions* as *actively sitting in front of the phone and waiting* while *non-blocking* could be seen as having *call forwarding* from
your home to your cell so you can do something else, while waiting for a call.

#### Subscriptions

A subscription is basically a predicate that filters based on your requirements:

Subscriptions can be lambda expression:

```java
user -> "Bob".equals(user.getName());
```

or method references

```java
User.BOB::equals
```

or of course concrete implementations:

```java
private UserSubscription user(String name) {
    return new UserSubscription(name);
}

private static class UserSubscription implements Subscription<User, Object> {

    private final String name;

    public UserSubscription(String name) {
        this.name = name;
    }

    @Override
    public boolean apply(User user) {
        return user.getName().equals(name);
    }
    
}
```

#### Blocking

Receiving messages in a blocking way is usually the easiest in terms of readability:

```java
User user = switchboard.receive(user("bob"), atLeastOnce(), within(10, SECONDS));
```

If a user called *Bob* is received within 10 seconds it will be returned otherwise a `TimeoutException` is thrown.
Additionally, since `receive` is a blocking operation, it is allowed to throw `InterruptedException`.

#### Non-blocking

Receiving messages in a non-blocking way is usually required if you need to subscribe to multiple different messages:

```java
Future<User> future = switchboard.subscribe(user("bob"), atLeastOnce());

future.get(); // wait forever
future.get(10, SECONDS); // wait at most 10 seconds
```

#### Subscription Modes

When subscribing to message you can specify one of the following modes. They have different characteristics in terms of termination and success conditions:

| Mode            | Termination | Success  |
|-----------------|-------------|----------|
| `atLeast(n)`    | `m >= n`    | `m >= n` |
| `atLeastOnce()` | `m >= 1`    | `m >= 1` |
| `atMost(n)`     | `m > n`     | `m <= n` |
| `exactlyOnce()` | `m > 1`     | `m == 1` |
| `never()`       | `m > 0`     | `m == 0` |
| `times(n)`      | `m > n`     | `m == n` |

**Note**: Be ware that `exactlyOnce()` and `times(n)` have termination conditions that require to wait till the end of the timeout to ensure its success 
condition holds true, e.g. `switchboard.receive(user("Bob"), exactlyOnce(), within(2, MINUTES))` will wait full 2 minutes in case it received 0 or 1 user 
called *Bob*. In case two or more users are received, it will terminate early and fail.

### Sending messages

You send messages by placing a *Deliverable* on the switchboard, either a [*message*](#message) or a [*failure*](#failure). Additionally one specifies a
[*delivery mode*](#delivery-modes) to indicate how the message will be distributed across relevant subscriptions.

#### Message

A message will be returned to active subscriptions:

```java
switchboard.send(message(bob, broadcast()));
```

#### Failure

Failures will throw their corresponding exception on the receiving thread. This can be useful to indicate a broken event channel or a missing feature which
should fail all relevant tests:

```java
switchboard.send(failure(bob, first(), new UnsupportedOperationException()));
```

#### Delivery Modes

Sending messages of any kind can be customized using the following delivery modes. It's their task to select the correct number of receivers among all current
subscriptions:

| Mode          | Delivers to            |
|---------------|------------------------|
| `directly()`  | the only subscription  |
| `broadcast()` | all subscriptions      |
| `first()`     | the first subscription |

*Direct delivery* is comparable to a normal phone call, i.e. one sender and one receiver. *Broadcast* on the other hand is more like a conference call, i.e.
one sender but many receivers. *First* might be (*and this is stretching the metaphor quite a bit*) a sales agent which calls people from a list, one at a time.

#### Workers, Resources and Queue Consumers

```java
class UserWorker implements Runnable {

    private final Switchboard switchboard;

    UserWorker(Switchboard switchboard) {
        this.switchboard = switchboard;
    }

    @Override
    public void run() {
        final List<String> names = switchboard.inspect(User.class, String.class);
        findUsersByNames(names).stream().forEach(switchboard::send);
    }

    private List<User> findUsersByNames(List<String> names) {
        // obviously fake
        return Collections.emptyList();
    }

}
```

#### Hints

The worker's task then is to inspect the switchboard to find all subscriptions for `User`s and fetch their hints, i.e. their names. The worker may then use 
some optimized way to fetch multiple users at once and sends the results back to the switchboard. 

Workers can utilize subscription hints to reduce the number of calls to retrieve message sources. This may be useful in some circumstances. Feel free to ignore 
hints and bulk fetch and send messages into the switchboard.

### Recording messages

Switchboard has an answering machine builtin. That means any message that arrives without anyone receiving it right away will be recorded and delivered as
soon as at least one receiver starts listening. This is especially useful if your tests need to listen to multiple messages and their order is not guaranteed.

```java
switchboard.send(message("foo", directly()));

String string = switchboard.receive("foo"::equals, atLeastOnce(), within(10, SECONDS));
```

The receiver will get the message immediately upon subscription.

## Attributions

![Creative Commons License](http://i.creativecommons.org/l/by-nc-sa/2.0/80x15.png)
[Meals at all Hours](https://www.flickr.com/photos/justininsd/7888302222/) by 
[Justin Brown](https://www.flickr.com/photos/justininsd/) is licensed under
[Attribution-NonCommercial-ShareAlike 2.0 Generic](https://creativecommons.org/licenses/by-nc-sa/2.0/).

## License

Copyright [2015] Zalando SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
