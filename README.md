# Switchboard: In-Process Message Router

[![Switchboard](docs/switchboard.jpg)](https://www.flickr.com/photos/justininsd/7888302222/)

[![Stability: Active](https://masterminds.github.io/stability/active.svg)](https://masterminds.github.io/stability/active.html)
![Build Status](https://github.com/whiskeysierra/switchboard/workflows/build/badge.svg)
[![Coverage Status](https://img.shields.io/coveralls/whiskeysierra/switchboard/master.svg)](https://coveralls.io/r/whiskeysierra/switchboard)
[![Code Quality](https://img.shields.io/codacy/grade/c6117b0da34448b4823c37d53cec9109/master.svg)](https://www.codacy.com/app/whiskeysierra/switchboard)
[![Javadoc](http://javadoc.io/badge/org.zalando/switchboard.svg)](http://www.javadoc.io/doc/org.zalando/switchboard)
[![Release](https://img.shields.io/github/release/whiskeysierra/switchboard.svg)](https://github.com/whiskeysierra/switchboard/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/switchboard.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/switchboard)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/whiskeysierra/switchboard/master/LICENSE)

> **Switchboard** noun, /swɪtʃ bɔːɹd/: The electronic panel that is used to direct telephone calls to the desired recipient.

An in-process message router that helps block on asynchronous events.
    
- **Technology stack**: Java 11+
- **Status**:  Beta, ported from an internal implementation that is used in production

## Example

```java
server.save("bob");
User user = switchboard.subscribe(userCreated("bob"), atLeastOnce(), ofSeconds(10)).get();
```

## Features

- makes asynchronous interactions more easily
- simple, extensible API
    
## Origin

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

## Dependencies

- Java 11 or higher

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>switchboard</artifactId>
    <version>${switchboard.version}</version>
    <scope>test</scope>
</dependency>
```

## Usage

Any communication via *Switchboard* consists of two parts: [Receiving](#receiving-messages) and [sending messages](#sending-messages).

### Receiving messages

You receive messages by [subscribing](#subscriptions) to it. Subscriptions can be done either in a [blocking](#blocking) or [non-blocking](#non-blocking) 
fashion. Additionally one specifies a [*subscription mode*](#subscription-modes) to indicate how much messages are going to be consumed.

Think of *blocking subscriptions* as *actively sitting in front of the phone and waiting* while *non-blocking* could be seen as having *call forwarding* from
your home to your cell so you can do something else, while waiting for a call.

#### Specifications

A specification is basically a predicate that filters based on your requirements:

Specifications can be lambda expression:

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
    return new UserSpecification(name);
}

private static final class UserSubscription implements Specification<User> {

    private final String name;

    public UserSubscription(final String name) {
        this.name = name;
    }

    @Override
    public boolean apply(final User user) {
        return user.getName().equals(name);
    }
    
}
```

Receiving messages in a non-blocking way is usually required if you need to subscribe to multiple different messages:

```java
Future<User> future = switchboard.subscribe(user("bob"), atLeastOnce(), ofSeconds(10));

future.get(); // wait 10 seconds
future.get(5, SECONDS); // wait at most 5 seconds
```

#### Subscription Modes

When subscribing to message you can specify one of the following modes. They have different characteristics in terms of
termination and success conditions:

| Mode            | Termination | Success  | 
|-----------------|-------------|----------|
| `atLeast(n)`    | `m >= n`    | `m >= n` |
| `atLeastOnce()` | `m >= 1`    | `m >= 1` |
| `atMost(n)`     | `m > n`     | `m <= n` |
| `atMostOnce()`  | `m > 1`     | `m <= 1` |
| `exactlyOnce()` | `m > 1`     | `m == 1` |
| `never()`       | `m > 0`     | `m == 0` |
| `times(n)`      | `m > n`     | `m == n` |

**Note**: Be ware that only `atLeast(n)` and `atLeastOnce()` have conditions that allow early termination for success
cases before the timeout is reached. All others will wait for the timeout to ensure its success condition holds true. 

### Sending messages

You send messages by placing a *Deliverable* on the switchboard, e.g. a [*message*](#message):

```java
switchboard.publish(message(bob));
```

Switchboard has an answering machine builtin. That means any message that arrives without anyone receiving it right away will be recorded and delivered as
soon as at least one receiver starts listening. This is especially useful if your tests need to listen to multiple messages and their order is not guaranteed.

```java
switchboard.publish(message("foo", directly()));

String string = switchboard.subscribe("foo"::equals, atLeastOnce(), ofSeconds(10));
```

The subscriber will get the message immediately upon subscription.

## Getting Help

If you have questions, concerns, bug reports, etc., please file an issue in this repository's [Issue Tracker](../../issues).

## Getting Involved/Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change. For
more details, check the [contribution guidelines](.github/CONTRIBUTING.md).

## Alternatives

- [Awaitility](https://github.com/awaitility/awaitility) is a small Java DSL for synchronizing asynchronous operations
- [Guava's EventBus](https://github.com/google/guava/wiki/EventBusExplained)  
   See [example implementation](src/test/java/org/zalando/switchboard/eventbus) with *at-least-once* semantics

## Credits and references

![Creative Commons License](http://i.creativecommons.org/l/by-nc-sa/2.0/80x15.png)
[Meals at all Hours](https://www.flickr.com/photos/justininsd/7888302222/) by 
[Justin Brown](https://www.flickr.com/photos/justininsd/) is licensed under
[Attribution-NonCommercial-ShareAlike 2.0 Generic](https://creativecommons.org/licenses/by-nc-sa/2.0/).
