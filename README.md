statemachine
============

This is a simple state machine framework used to support various crowdsourcing projects where customized workflows were necessary.

It is configurable a JSON so that it can be defined by a graphical web interface.

The state machine operates on Stateful objects. The objects themselves don't know their state machine. Rather, the state machine is defined as a generic and visits the objects they manage to advance their state. This allows the object to be compatible with state machines that evolve over time. The state machine is serializable, expressed as JSON. This makes it possible for the state machine to be versioned.

Please see the IssueTrackerTest.java file for an example of how this state machine is applied to a simple bug tracking.


