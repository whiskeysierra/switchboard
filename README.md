# Switchboard

[![Switchboard](docs/switchboard.jpg)](https://www.flickr.com/photos/justininsd/7888302222/)

[![Build Status](https://img.shields.io/travis/zalando/switchboard.svg)](https://travis-ci.org/zalando/switchboard)
[![Coverage Status](https://img.shields.io/coveralls/zalando/switchboard.svg)](https://coveralls.io/r/zalando/switchboard)
[![Release](https://img.shields.io/github/release/zalando/switchboard.svg)](https://github.com/zalando/switchboard/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/switchboard.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/switchboard)

An in-process event router that helps to write simple, asynchronous, state-based and collaboration tests. 
    
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
asynchronous events.

The term *switchboard* refers to old-style telephone switchboards, i.e. big communication devices handled by a switchboard operator that allow to connect
multiple parties via telephone lines, usually one caller and one receiver.
 
In our case those parties are threads and they *talk* to each other by passing messages.

## Usage

Any communication via *Switchboard* consists of two parts: [Receiving](#receiving-messages) and [sending messages](#sending-messages).

### Receiving messages

You receive messages by [subscribing](#subscriptions) to it. Subscriptions can be done either in a [blocking](#blocking) or [non-blocking](#non-blocking) fashion. Additionaly one can specify a [*mode*](#modes) to indicate how much messages are going to be consumed.

#### Subscriptions

A subscription is bascially a predicate that filters based on your requirements:

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
Switchboard board = Switchboard.create();
User user = board.receive(user("bob"), atLeastOnce(), in(10, SECONDS));
```

If a user called *Bob* is received within 10 seconds it will be returned otherwise a `TimeoutException` is thrown.
Additionally, since `receive` is a blocking operation, it is allowed to throw `InterruptedException`.

#### Non-blocking

#### Modes

| Mode            | Type         | Termination | Success  |
|-----------------|--------------|-------------|----------|
| `atLeast(n)`    | non-blocking | `m >= n`    | `m >= n` |
| `atLeastOnce`   | non-blocking | `m >= 1`    | `m >= 1` |
| `atMost(n)`     | non-blocking | `m > n`     | `m <= n` |
| `exactlyOnce()` | blocking     | `m > 1`     | `m == 1` |
| `never()`       | non-blocking | `m > 0`     | `m == 0` |
| `times(n)`      | blocking     | `m > n`     | `m == n` |

### Sending messages

#### Modes

 - DIRECT
 - BROADCAST
 - FIRST

Until now we only implemented half of the process. We registered on the switchboard with a subscription but since nobody is providing any actual events our 
test will never succeed. This is the task of a sender:

```java
class UserWorker implements Runnable {

    private final Switchboard board;

    UserWorker(Switchboard board) {
        this.board = board;
    }

    @Override
    public void run() {
        final List<String> names = board.inspect(User.class, String.class);
        findUsersByNames(names).stream().forEach(board::send);
    }

    private List<User> findUsersByNames(List<String> names) {
        // obviously fake
        return Collections.emptyList();
    }

}
```

The worker requires the `switchboard` instance which is usually shared via some kind of dependency injection context, e.g. Spring. It also requires some scheduling, e.g. Quartz or just a single-threaded [`ScheduledExecutorService`](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executors.html#newSingleThreadScheduledExecutor\(\)).

#### Hints

The worker's task then is to inspect the switchboard to find all subscriptions for `User`s and fetch their hints, i.e. their names. The worker may then use some optimized way to fetch multiple users at once and sends the results back to the switchboard. 

Workers can utilize subscription hints to reduce the number of calls to retrieve the events. This may be useful in some circumstances. Feel free to ignore hints and bulk fetch and send events into the switchboard.

### Recording messages

Switchboard has an answering machine builtin. That means any message that arrives without anyone receiving it right away will be recorded and delivered as
soon as at least one receiver starts listening:

```java
switchboard.send("foo", DIRECT);

String s = switchboard.receive("foo"::equals, atLeastOnce(), in(10, SECONDS));
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
