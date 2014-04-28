package statemachine;

import static common.Utilities.isEmpty;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import statemachine.StateMachine.Trigger;

import common.NamedObjectBuilder;


/**
 * Builder used to create state machines since the construction process is very
 * complex.
 *
 * @author patrick
 *
 * @param <O> the object type to be managed by the state machine
 */
public final class StateMachineBuilder<O extends StatefulObject, S extends StateMachine<O>> extends NamedObjectBuilder<StateMachineBuilder<O,S>,S> {

    /*
     * printable labels and descriptions for states and transitions
     */
    private final Map<String, String> stateNames = new HashMap<String, String>();
    private final Map<String, String> stateDescriptions = new HashMap<String, String>();
    private final Map<String, String> transitionNames = new HashMap<String, String>();
    private final Map<String, String> transitionDescriptions = new HashMap<String, String>();

    /*
     * state machine logic
     */
    private String initialTransition = null;
    private final LinkedHashSet<String> initialTransitions = new LinkedHashSet<String>();
    private final Map<String, String> targetStates = new HashMap<String, String>();
    private final Map<String, Class<Trigger<O,?>>> preTriggers = new HashMap<String, Class<Trigger<O,?>>>();
    private final Map<String, Class<Trigger<O,?>>> postTriggers = new HashMap<String, Class<Trigger<O,?>>>();
    private final Map<String, Set<String>> validTransitions = new HashMap<String, Set<String>>();
    private final Map<String, String> defaultTransitions = new HashMap<String, String>();
	private TransitionSelector<O> transitionSelector = null;
    private final Map<String, Map<String,Object>> transitionProperties = new HashMap<String, Map<String,Object>>();




    /**
     * convenience method to define a state machine via its transitions
     *
     * @param name
     *            the name of the transition
     * @param description
     *            the description of the transition
     * @param fromState
     *            the source state for this transition
     * @param toState
     *            the target state
     * @param toStateDescription
     *            the target state description
     * @param preTrigger
     *            the pre-trigger to associate with the transition
     * @param postTrigger
     *            the post-trigger to associate with the transition
     * @return this builder
     */
    public final StateMachineBuilder<O,S> withTransition(final String name,
            final String description, final String fromState,
            final String toState, final String toStateDescription,
            final Map<String,Serializable> onCondition,
            final Class<Trigger<O,?>> preTrigger,
            final Class<Trigger<O,?>> postTrigger) {
        return withTransition(name, description, new String[]{fromState}, toState, toStateDescription, onCondition, preTrigger, postTrigger);
    }


    /**
     * convenience method to define a state machine via its transitions
     *
     * @param name
     *            the name of the transition
     * @param description
     *            the description of the transition
     * @param fromStates
     *            the source state(s) for this transition
     * @param toState
     *            the target state
     * @param toStateDescription
     *            the target state description
     * @param preTrigger
     *            the pre-trigger to associate with the transition
     * @param postTrigger
     *            the post-trigger to associate with the transition
     * @return this builder
     */
    public final StateMachineBuilder<O,S> withTransition(final String name,
            final String description, final String[] fromStates, final String toState,
            final String toStateDescription, final Map<String,Serializable> onCondition,
            final Class<Trigger<O,?>> preTrigger, final Class<Trigger<O,?>> postTrigger ) {
        return withTransition(name, description, Arrays.asList(fromStates), toState, toStateDescription, onCondition, preTrigger, postTrigger);
    }


    /**
     * define a state machine via its transitions
     *
     * @param name
     *            the name of the transition
     * @param description
     *            the description of the transition
     * @param fromStates
     *            the source state(s) for this transition
     * @param toState
     *            the target state
     * @param toStateDescription
     *            the target state description
     * @param preTrigger
     *            the pre-trigger to associate with the transition
     * @param postTrigger
     *            the post-trigger to associate with the transition
     * @return this builder
     */
    public final StateMachineBuilder<O,S> withTransition(final String name,
            final String description, final List<String> fromStates,
            final String toState, final String toStateDescription,
            final Map<String,Serializable> params,
            final Class<Trigger<O,?>> preTrigger,
            final Class<Trigger<O,?>> postTrigger) {
        /*
         * transitions are defined by the destination state.
         */
        // check that a transition name was provided
        if( isEmpty( name ) ) {
            throw new IllegalArgumentException( "transition name cannot be empty" );
        }
        // check to see that a toState was provided
        if( isEmpty( toState ) ) {
            throw new IllegalArgumentException( "target state cannot be empty" );
        }

        // capture the label, or pretty name, for the transition
        final String transitionName = name.trim();
        // capture the transition key
        final String transitionKey = toKey(transitionName);

        // check to see if transition key already exists
        if( transitionNames.containsKey( transitionKey ) ) {
            throw new IllegalArgumentException(
                    String.format(
                            "duplicate transitions. trying to add '%s', but '%s' already exists.",
                            transitionName, transitionNames.get(transitionKey)));
        }

        // add transition name to collection
        transitionNames.put( transitionKey, transitionName );

        // do the same for descriptions
        if( !isEmpty( description ) ) {
            transitionDescriptions.put( transitionKey, description.trim() );
        }

        // capture the label, or pretty name, for the target state
        final String toStateName = toState.trim();
        // capture the state key
        final String stateKey = toKey(toState);

        // add state name to collection
        if( !stateDescriptions.containsKey( stateKey ) ) {
            // if this is the first time we see the state add the name to the state name collection
            stateNames.put( stateKey, toStateName );
        }

        // if a description is provided and none are already set for the state, add the description to the state description collection
        if( !isEmpty(toStateDescription) && isEmpty(stateDescriptions.get(stateKey)) ) {
            stateDescriptions.put( stateKey, toStateDescription.trim() );
        }

        // set the target state for the transition
        assert transitionKey != null;
        assert stateKey != null;
        targetStates.put( transitionKey, stateKey );



        // if pre or post triggers are defined for the transition, add them to the state machine
        if( preTrigger != null ) {
            preTriggers.put( transitionKey, preTrigger );
        }
        if( postTrigger != null ) {
            postTriggers.put( transitionKey, postTrigger );
        }


        // if a fromState is defined, then add transition to set of valid transitions for that state
        Set<String> validStateTransitions = null;
        if( (fromStates == null) || fromStates.isEmpty() ) {
            // if no from state was provided, then this transition is an entry point for the state machine.
            // set the transitions collection to the initial transitions collection
            validStateTransitions = initialTransitions;

            // if this is the first entry point encountered, set it as the default initial transition.
            if( initialTransition == null ) {
                initialTransition = transitionKey;
            }

            // now that we have a transition collection, add the transition key to it.
            validStateTransitions.add( transitionKey );
        } else {
            for( final String from : fromStates ) {
                if( isEmpty( from ) ) {
                    // if no from state value empty, then this transition is an entry point for the state machine.
                    // set the transitions collection to the initial transitions collection
                    validStateTransitions = initialTransitions;

                    // if this is the first entry point encountered, set it as the default initial transition.
                    if( initialTransition == null ) {
                        initialTransition = transitionKey;
                    }
                } else {
                    // if a from state was provided, then add the transition to the set
                    // of transitions that are defined for that state
                    final String fromStateName = from.trim();
                    final String fromStateKey = toKey(fromStateName);

                    // if the state is new, add it to the set of state names.
                    if( !stateNames.containsKey( fromStateKey ) ) {
                        stateNames.put( fromStateKey, fromStateName );
                    }

                    // get the set of transitions allowed for that state and set the
                    // transitions collection to the collection of allowed transitions
                    // for that state.
                    validStateTransitions = this.validTransitions.get( fromStateKey );
                    if( validStateTransitions == null ) {
                        // if none are defined, create a collection to contain the allowed transitions from that state
                        validStateTransitions = new LinkedHashSet<String>();
                        // and add it to the valid transitions collection for the state machine
                        this.validTransitions.put( fromStateKey, validStateTransitions );
                    }

                    // if this is the first transition defined from a state, set it as the default transition for that state
                    if( validStateTransitions.size() == 0 ) {
                        defaultTransitions.put( fromStateKey, transitionKey );
                    }
                }

                // now that we have a transition collection, add the transition key to it.
                validStateTransitions.add( transitionKey );
            }
        }

        // if transition selection conditions were provided, add then to the states
        if( (params != null) && !params.isEmpty() ) {
        	Map<String,Object> transitionProperties = this.transitionProperties.get( transitionKey );
    		if( transitionProperties == null ) {
    			transitionProperties = new LinkedHashMap<String, Object>();
    			this.transitionProperties.put( transitionKey, transitionProperties );
    		}
    		transitionProperties.putAll( params );
        }

        // return the builder.
        return this;
    }

	/**
	 * @param selector
	 * @return
	 */
	public final StateMachineBuilder<O,S> withTransitionSelector(final TransitionSelector<O> selector) {
		this.transitionSelector = selector;
		return this;
	}

    /**
     * a convenience method that
     * @param name
     * @return
     */
    protected final String toKey(final String name) {
        final StringBuilder key = new StringBuilder();
        for( final char c : name.toCharArray() ) {
            if( Character.isJavaIdentifierStart(c) ) {
                key.append( Character.toLowerCase( c ) );
            }
        }
        return key.toString();
    }


    /* (non-Javadoc)
     * @see statemachine.NamedObjectBuilder#validate()
     */
    @Override final protected void validate() {
        // call the super class's validator to make sure a name was set for the state machine
        super.validate();

        /*
         * verify state machine initialization
         */
        if( initialTransition == null ) {
            throw new IllegalStateException( "state machine must have an initial transition" );
        }
        if( transitionNames.get( initialTransition ) == null ) {
            throw new IllegalStateException( "initial transition must be defined" );
        }
        if( !initialTransitions.contains( initialTransition ) ) {
            throw new IllegalStateException( "the default initial transition must be defined in the initial transitions collection" );
        }
        final String initialState = targetStates.get( initialTransition );
        if( initialState == null ) {
            throw new IllegalStateException( "initial transition must define a target state" );
        }
        if( stateNames.get( initialState ) == null ) {
            throw new IllegalStateException( "initial state must be defined" );
        }


        /*
         * verify that all state and transition keys in state machine are defined and match those used on the names collections
         */
        validateKeys("invalid initial transitions. no such transitions: ", initialTransitions, transitionNames);
        validateKeys("invalid default transitions. no such states: ", defaultTransitions.keySet(), stateNames);
        validateKeys("invalid default transitions. no such transitions: ", defaultTransitions.values(), transitionNames);
        validateKeys("invalid pre-triggers. no such transitions: ", preTriggers.keySet(), transitionNames);
        validateKeys("invalid post-triggers. no such transitions: ", postTriggers.keySet(), transitionNames);
        validateKeys("invalid transition. no such transitions: ", targetStates.keySet(), transitionNames);
        validateKeys("invalid transition. no such states: ", targetStates.values(), stateNames);
        validateKeys("invalid transitions. no such states: ", validTransitions.keySet(), stateNames);
        final HashSet<String> transitions = new HashSet<String>( transitionNames.size() );
        for( final Set<String> t : validTransitions.values() ) {
            transitions.addAll( t );
        }
        validateKeys("invalid transition. no such transitions: ", transitions, transitionNames);
        validateKeys("invalid transition description. no such transitions: ", transitionDescriptions.keySet(), transitionNames);
        validateKeys("invalid state description. no such states: ", stateDescriptions.keySet(), stateNames);


        /*
         * make sure that all states can be reached and that all transitions are used
         */
        final Set<String> remainingStates = new HashSet<String>( stateNames.keySet() );
        final Set<String> remainingTransitions = new HashSet<String>( transitionNames.keySet() );
        final Set<String> endStates = new HashSet<String>();
        for( final String init : initialTransitions ) {
            remainingTransitions.remove( init );
            validateTransitions( targetStates.get(init), remainingStates, remainingTransitions, endStates );
        }
        if( remainingStates.size() > 0 ) {
            throw new IllegalStateException( "all states in state machine should be reachable" );
        }
        if( remainingTransitions.size() > 0 ) {
            throw new IllegalStateException( "all transitions in state machine should be used at least once" );
        }
        if( endStates.isEmpty() ) {
            throw new IllegalStateException( "there should be at least one end state in the state machine" );
        }


        /*
         *  verify that every initial transitions target different states.
         */
        verifyTargetDestinationsForState("state machine entry points", initialTransitions);


        /*
         *  verify that every allowed transitions for each state goes to different target states.
         */
        for( final Entry<String, Set<String>> entry : validTransitions.entrySet() ) {
            verifyTargetDestinationsForState(entry.getKey(), entry.getValue());
        }


        /*
         * verify the default or main flow thru state machine
         */
        // start with initial state
        String state = initialState;
        final int limit = 0;
        Set<String> subset = null;
        String defaultTransition = null;
        while( (validTransitions.get(state) != null) && (limit < stateNames.size()) ) {

        	subset = validTransitions.get( state );

            // for the default transition defined for that state
            defaultTransition = defaultTransitions.get( state );
            // check to make sure the default transition is defined as a valid transition in the from state
            if( !subset.contains(defaultTransition ) ) {
                throw new IllegalStateException( String.format("default transition '%s' must be included in set of valid transitions for state '%s'", defaultTransition, state) );
            }
            // change state to the target state for the default transition
            state = targetStates.get( defaultTransition );
            // repeat for the next state
            // until we reach an end state.
        }
        // if we exit the loop for any other reason, raise an exception
        if( validTransitions.get(state) != null ) {
            throw new IllegalStateException( "default transitions should lead to end state." );
        }

    }

    /**
     * convenience method to check that transitions reach unique destinations.
     *
     * @param from
     *            the source state for the transitions
     * @param transitions
     *            the transitions to check
     */
    private void verifyTargetDestinationsForState(final String fromState,
            final Set<String> transitions) {
        // remember the destinations we have already visited
        final Set<String> destinations = new HashSet<String>();
        String to = null;
        // for each transition
        for( final String transition : transitions ) {
            to = targetStates.get( transition );
            // if we have already seen that destination, raise an excpetion
            if( destinations.contains( to ) ) {
                throw new IllegalStateException(
                        String.format(
                                "valid transitions from '%s' cannot have the same target '%s' state as other transitions.",
                                fromState, to) );
            }
            // otherwise remember it
            destinations.add( to );
        }
    }

    /**
     * recursive check that makes sure that the state machine can reach every
     * state defined and that there are no dead states.
     *
     * @param currentState
     *            the current state to check
     * @param remainingStates
     *            states that still need to be reached from this state
     * @param remainingTransitions
     *            transitions to use to reach these states
     * @param endStates
     *            end states found so that they can be removed from remaining
     *            states to reach
     */
    private void validateTransitions(final String currentState, final Set<String> remainingStates, final Set<String> remainingTransitions, final Set<String> endStates) {
        // remove the current state from the remaining states since we are there
        remainingStates.remove( currentState );

        // see if the current state is an end state. if so, add it to the end state collection and exit.
        String state = null;
        final Set<String> transitions = validTransitions.get( currentState );
        if( transitions == null ) {
            endStates.add( currentState );
            return;
        }

        // otherwise, for each transition defined, record the states that can be
        // reached
        for( final String transition : transitions ) {
            // indicate that the transition has been analyzed and remove it from
            // the set of remaining transitions to analyze
            remainingTransitions.remove( transition );
            // then recursively analyze the target state for that transition if it has not yet been analyzed
            state = targetStates.get(transition);
            if( remainingStates.contains( state ) ) {
                validateTransitions( state, remainingStates, remainingTransitions, endStates );
            }
        }
    }

    /**
     * checks to see that all the keys in keysToCheck are defined in the
     * definedKeys
     *
     * @param label
     *            the error message to display if keysToCheck contains keys that
     *            don't exist in definedKeys
     * @param keysToCheck
     *            the collection to check
     * @param definedKeys
     *            the known keys
     */
    private void validateKeys(final String label, final Collection<String> keysToCheck, final Map<String, String> definedKeys) {
        // clone the keys from the collection
        final HashSet<String> keys = new HashSet<String>( keysToCheck );

        // for each key ...
        final Iterator<String> items = keys.iterator();
        String item = null;
        while( items.hasNext() ) {
            item = items.next();
            // ... if they exist in the definedKeys collection ...
            if( definedKeys.containsKey( item ) ) {
                // ... remove them
                items.remove();
            }
        }

        // if any keys remain, then keysToCheck contains keys that don't exist in definedKeys
        if( keys.size() > 0 ) {
            // raise an exception to indicate the error.
            throw new IllegalStateException( label + keys.toString() );
        }
    }



    /* (non-Javadoc)
     * @see common.Builder#create()
     */
    @SuppressWarnings({ "unchecked" })
    @Override public final S create() throws MalformedStateMachineException {
        validate();
        return (S) new StateMachine<O>(
                super.getName(), super.getDescription(),
                stateNames, stateDescriptions,
                transitionNames, transitionDescriptions, transitionProperties,
                initialTransition, initialTransitions,
                validTransitions, defaultTransitions,
                targetStates, transitionSelector,
                preTriggers, postTriggers
                );
    }

    /**
     * build transitions from a Map tree such as something parsed from JSON
     *
     * @param transitions
     *            the transitions, in nested map structure, to add to the state
     *            machine
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    public final StateMachineBuilder<O,S> withTransitions(final List<Map<String, ?>> transitions) {
        Map<String,?> to = null;
        List<String> from = null;
        for(final Map<String, ?> transition : transitions ) {
            to = (Map<String, ?>) transition.get( "to" );
            from = (List<String>) transition.get( "from" );
            withTransition(
                    (String) transition.get( "name" ),
                    (String) transition.get( "description" ),
                    from,
                    (String) to.get( "name" ),
                    (String) to.get( "description" ),
                    (Map<String,Serializable>) transition.get( "on" ),
                    createTrigger((String) transition.get( "pre-trigger" )),
                    createTrigger((String) transition.get( "post-trigger" ))
                    );
            }
        return this;
    }

    /**
     * completely clear out state machine transitions and state information
     *
     * @return a clean state machine builder
     */
    public final StateMachineBuilder<O,S> withoutTransitions() {
        // remove transition declarations
        this.transitionNames.clear();
        this.transitionDescriptions.clear();
        this.targetStates.clear();

        // remove transition references
        this.defaultTransitions.clear();
        this.initialTransition = null;
        this.initialTransitions.clear();
        for( final Entry<String, Set<String>> stateTransitions : validTransitions.entrySet() ) {
            stateTransitions.getValue().clear();
        }
        this.preTriggers.clear();
        this.postTriggers.clear();
        this.validTransitions.clear();

        // remove states
        this.stateNames.clear();
        this.stateDescriptions.clear();

        return this;
    }


    /**
     * completely clear out state machine transitions and state information
     *
     * @return a clean state machine builder
     */
    public final StateMachineBuilder<O,S> withoutTransition( final String name ) {
        // make sure something was given
        if( isEmpty(name) ) {
            throw new IllegalArgumentException( "must provide transition name" );
        }

        // make sure we know this transition
        String transitionKey = null;
        final String s = name.trim();
        for( final Entry<String, String> transition : transitionNames.entrySet() ) {
            if( transition.getKey().equalsIgnoreCase( s ) || transition.getValue().equalsIgnoreCase( s ) ) {
                transitionKey = transition.getKey();
                break;
            }
        }
        if( transitionKey == null ) {
            throw new IllegalArgumentException( "invalid transition: " + s );
        }


        // remove transition declarations
        this.transitionNames.remove( transitionKey );
        this.transitionDescriptions.remove( transitionKey );
        this.targetStates.remove( transitionKey );

        /*
         *  remove transition references
         */
        // remove transition from the initial transitions and adjust the default initial transition if necessary
        initialTransition = removeTransition(initialTransitions, transitionKey);
        // remove transition from the allowed state transition constructs
        String state = null;
        for( final Entry<String, Set<String>> stateTransitions : validTransitions.entrySet() ) {
            state = stateTransitions.getKey();
            // remove transition from the allowed transitions from the state and adjust the default transition if necessary
            defaultTransitions.put( state, removeTransition( stateTransitions.getValue(), transitionKey ) );
        }

        // remove any triggers associated with transition
        this.preTriggers.remove( transitionKey );
        this.postTriggers.remove( transitionKey );

        return this;
    }

    /**
     * utility method that removes transition from the transition set, and returns a new default transition if necessary
     * @param transitions the collection to process
     * @param transition the transition to remove
     * @return the new default transition for the collection
     */
    private String removeTransition(final Set<String> transitions, final String transition) {
        // adjust default transition if necessary
        String nextDefaultTransition = null;
        for( final String t : transitions ) {
            if( t.equals( transition ) ) {
                continue;
            }
            nextDefaultTransition = t;
            break;
        }

        // remove the transition from the collection
        transitions.remove( transition );

        // return the next transition
        return nextDefaultTransition;
    }

    /**
     * utility method that looks for and creates an instance of the trigger from its public constructor
     * @param triggerClass the trigger type to create
     * @return an instance of the trigger
     * @throws IllegalArgumentException if trigger instance could not be created
     */
    @SuppressWarnings("unchecked")
    public final Class<Trigger<O,?>> createTrigger(final String triggerClass ){
        try {
            return (Class<Trigger<O,?>>) (isEmpty( triggerClass ) ? null : Class.forName( triggerClass ));
        } catch (final Exception e) {
           throw new IllegalArgumentException( "invalid trigger: " + triggerClass, e );
        }
    }


}