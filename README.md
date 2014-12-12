# Circuit

[![Circuit Board](docs/circuit.jpg)](http://pixabay.com/en/board-electronics-computer-453758/)

[![Build Status](https://travis-ci.org/whiskeysierra/circuit.svg)](https://travis-ci.org/whiskeysierra/circuit)
[![Coverage Status](https://coveralls.io/repos/whiskeysierra/circuit/badge.png)](https://coveralls.io/r/whiskeysierra/circuit)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.zalando/zalando-circuit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.zalando/zalando-circuit)
    
An in-process, publish/subscribe-style event router that helps to write simple, asynchronous, state-based and collaboration tests. 
    
## Concept
 
asynchronous state changes in different places, e.g.:

- email to the customer
- data export to warehouse

The whole problem is easy in a synchronous environment:

```java
ProcessResult result = process.run(parcel);
ParcelExport export = result.getExport();
```
    
It get's a lot more difficult if the results are not immediately available

```java
interface ParcelProcess {
    void run(Parcel parcel);
}
```

```java
process.run(parcel);
// where do I get the results from?
```

```java
ParcelExport export = circuit.receive(exportedParcel("738235167"), 5, MINUTES);
```
    
Has a similar *feeling* compared to the synchronous version, but abstracts the asynchronous work.