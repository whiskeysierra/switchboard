=======
Circuit
=======

.. image:: https://travis-ci.org/whiskeysierra/circuit.svg
    :target: https://travis-ci.org/whiskeysierra/circuit
    
.. image:: https://coveralls.io/repos/whiskeysierra/circuit/badge.png
    :target: https://coveralls.io/r/whiskeysierra/circuit
    
.. image:: https://maven-badges.herokuapp.com/maven-central/de.zalando/zalando-circuit/badge.svg
    :target: https://maven-badges.herokuapp.com/maven-central/de.zalando/zalando-circuit
    
Concept
=======
 
asynchronous state changes in different places, e.g.:

- email to the customer
- data export to warehouse

The whole problem is easy in a synchronous environment:

.. code-block:: java

    ProcessResult result = process.run(parcel);
    ParcelExport export = result.getExport();
    
It get's a lot more difficult if the results are not immediately available
    
.. code-block:: java

    interface ParcelProcess {
        void run(Parcel parcel);
    }
    
    process.run(parcel);
    
    // where do I get the results from?
    
    
.. code-block:: java

    ParcelExport export = circuit.receive(exportedParcel("738235167"), 5, MINUTES);
    
Has a similar *feeling* compared to the synchronous version, but abstracts the asynchronous work.