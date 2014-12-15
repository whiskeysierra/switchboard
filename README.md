# Circuit

[![Circuit Board](docs/circuit.jpg)](http://pixabay.com/en/board-electronics-computer-453758/)

[![Build Status](https://travis-ci.org/whiskeysierra/circuit.svg)](https://travis-ci.org/whiskeysierra/circuit)
[![Coverage Status](https://coveralls.io/repos/whiskeysierra/circuit/badge.png)](https://coveralls.io/r/whiskeysierra/circuit)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.zalando/zalando-circuit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.zalando/zalando-circuit)
    
An in-process, publish/subscribe-style event router that helps to write simple, asynchronous, state-based and collaboration tests. 
    
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
Circuit circuit = new DefaultCircuit();
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

The worker requires the  `circuit` instance which is usually shared via some kind of dependency injection context, e.g. Spring. It also requires some scheduling, e.g. Quartz or just a single-threaded [`ScheduledExecutorService`](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executors.html#newSingleThreadScheduledExecutor\(\)).

The worker's task then is to inspect the circuit to find all subscriptions for `User`s and fetch their hints, i.e. their names. The worker may then use some optimized way to fetch multiple users at once and sends the results back to the circuit. 

Workers can utilize subscription hints to reduce the number of calls to retrieve the events. This may be useful in some circumstances. Feel free to ignore hints and bulk fetch and send events into the circuit.
