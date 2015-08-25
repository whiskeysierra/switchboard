# Circuit

[![Circuit Board](docs/circuit.jpg)](http://pixabay.com/en/board-electronics-computer-453758/)

[![Build Status](https://img.shields.io/travis/zalando/circuit.svg)](https://travis-ci.org/zalando/circuit)
[![Coverage Status](https://img.shields.io/coveralls/zalando/circuit.svg)](https://coveralls.io/r/zalando/circuit)
[![Release](https://img.shields.io/github/release/zalando/circuit.svg)](https://github.com/zalando/circuit/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/circuit.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/circuit)

An in-process, publish/subscribe-style event router that helps to write simple, asynchronous, state-based and collaboration tests. 
    
## Dependency

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>circuit</artifactId>
    <version>${circuit.version}</version>
    <scope>test</scope>
</dependency>
```
    
## Concept

In any non-trivial application you'll most probably seen a process like this.

![Synchronous Interaction](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgU3luY2hyb25vdXMgSW50ZXJhY3Rpb24KCkNsaWVudC0-U2VydmVyOiBSZXF1ZXN0CgAKBi0-RGF0YWJhc2UAEAoACggAKAxzcG9uc2UALAkATwYADgs&s=napkin)
 
Since the call to the database is synchronous we can be sure it happened before the client got the response. Testing this case is therefore relatively straightforward:

```java
Response response = server.save(bob);
assertThat(database.getUser("bob"), is(not(nullValue())));
```

It all get's more difficult when you start to do time-consuming tasks in an asynchronous fashion:
 
![Asynchronous Interaction](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgQXN5bmNocm9ub3VzIEludGVyYWN0aW9uCgpDbGllbnQtPlNlcnZlcjogUmVxdWVzdAoACgYtPkRhdGFiYXNlAAgSADQGOiBSZXNwb25zZQoAIwgAQQwAFAc&s=napkin)
 
The test case from above may no longer work, since we may get the response back from the server before the database finished its part.

The idea of *Circuit* is to encapsulate the necessary polling/retry mechanism behind a small API that allows to write synchronous-style assertions for asynchronous events.

```java
Circuit circuit = Circuit.create();
User user = circuit.receive(user("bob"), 10, SECONDS);
```

The `receive` operation requires a `Subscription` and a timeout. It either returns the requested event or fails with a `TimeoutException`. A subscription is nothing more than a predicate with some additional metadata:

```java
private UserSubscription user(String name) {
    return new UserSubscription(name);
}

private static class UserSubscription extends TypedSubscription<User, String> {

    private final String name;

    public UserSubscription(String name) {
        this.name = name;
    }

    @Override
    public boolean apply(User user) {
        return user.getName().equals(name);
    }

    @Override
    public String getHint() {
        return name;
    }
    
}
```

Until now we only implemented half of the process. We registered on the circuit with a subscription but since nobody is providing any actual events our test will never succeed. This is the task of a worker:

```java
class UserWorker implements Runnable {

    private final Circuit circuit;

    UserWorker(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void run() {
        final List<String> names = circuit.inspect(User.class, String.class);
        findUsersByNames(names).stream().forEach(circuit::send);
    }

    private List<User> findUsersByNames(List<String> names) {
        // obviously fake
        return Collections.emptyList();
    }

}
```

The worker requires the `circuit` instance which is usually shared via some kind of dependency injection context, e.g. Spring. It also requires some scheduling, e.g. Quartz or just a single-threaded [`ScheduledExecutorService`](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executors.html#newSingleThreadScheduledExecutor\(\)).

The worker's task then is to inspect the circuit to find all subscriptions for `User`s and fetch their hints, i.e. their names. The worker may then use some optimized way to fetch multiple users at once and sends the results back to the circuit. 

Workers can utilize subscription hints to reduce the number of calls to retrieve the events. This may be useful in some circumstances. Feel free to ignore hints and bulk fetch and send events into the circuit.

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