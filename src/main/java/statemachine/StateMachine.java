package statemachine;

import static common.Utilities.isEmpty;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import common.BaseNamedObject;


/**
 * A state machine governs the transition that stateful objects can take.
 *
 * It is a named object because object types that have their state machines
 * defined this way are generally referred by a logical reference, usually a
 * name.
 *
 * @author patrick
 *
 * @param <S>
 *            the stateful object type managed by this state machine
 *
 */
public class StateMachine<S extends StatefulObject<S>> extends BaseNamedObject<StateMachine<S>> {
    private static final long serialVersionUID = 1L;

    /*
     * State Definitions
     */
    /**
     * when the state machine is constructed it will generate logical names to
     * use as keys to represent the states. this map maps the logical names to
     * the state labels that were provided.
     */
    private final Map<String, String>            stateNames;
    /**
     * map of descriptions to state logical, or reference, names.
     */
    private final Map<String, String>            stateDescriptions;

    /*
     * Transition Definitions
     */
    /**
     * when the state machine is constructed it will generate logical names to
     * use as keys to represent the transitions. this map maps the logical names
     * to the transition labels that were provided.
     */
    private final Map<String, String>            transitionNames;
    /**
     * map of descriptions to transition logical, or reference, names.
     */
    private final Map<String, String>            transitionDescriptions;

    /**
	 * additional transition properties defined in JSON file but not handled by
	 * the base state machine engine.
	 */
    private final Map<String, Map<String, Object>> transitionProperties;

    /**
     * when transitions are defined, they are defined relative to the
     * destination state that will be reached once the transition completes.
     * there is only one destination, or target state for each transition.
     * however, several transitions can define the same target state.
     */
    private final Map<String, String>            targetStates;

    /*
     * State Machine Initialization
     */
    /**
     * all the possible entry points into the state machine.
     */
    private final Set<String>                    initialTransitions;
    /**
     * a state machine must define at least one entry point. the first entry
     * point is defined as the the default initial transition for the state
     * machine. this field defines which of the initial transitions is the
     * default transition.
     */
    private final String                         initialTransition;

    /*
     * State Machine Transitions
     */
    /**
     * a map of state names and all the valid, or allowed, transitions from that
     * state. for each from/to state pairings, there should only be one
     * transition that defines that transition. a state may have 0, 1, or more
     * valid transitions defined.
     */
    final Map<String, Set<String>>               validTransitions;
    /**
     * a map of state names and the default transition for that state. if a
     * state has no transitions defined, then it will not have a default
     * transition defined in this map. however, if the state has one or more
     * transitions defined, then the first (or only) transition is declared as
     * the default transition.
     */
    final Map<String, String>                    defaultTransitions;

	/**
	 * logic that defines what transition the state machine should invoke as a
	 * result of transition data provided. used by
	 * <code>advance()</code> when given a transition data object.
	 */
	private final TransitionSelector<S> transitionSelector;


    /*
     * Triggers
     */
    /**
     * map of transition to pre-trigger. each transition may define at most 1
     * pre trigger.
     */
    private final Map<String, Class<Trigger<S,?>>> preTriggerRegistry;
    /**
     * map of transition to post-trigger. each transition may define at most 1
     * post trigger.
     */
    private final Map<String, Class<Trigger<S,?>>> postTriggerRegistry;

    /**
     * map of transition to pre-trigger. each transition may define at most 1
     * pre trigger.
     */
    private transient Map<String, Trigger<S,?>>    preTriggers;
    /**
     * map of transition to post-trigger. each transition may define at most 1
     * post trigger.
     */
    private transient Map<String, Trigger<S, ?>>    postTriggers;

    /**
     * @param config
     * @throws MalformedStateMachineException 
     */
    @SuppressWarnings("unchecked")
	protected StateMachine ( Map<String,Serializable> config ) throws MalformedStateMachineException {
    	this(
    			(String) config.get( "name" ),
    			(String) config.get( "description" ),
    			(Map<String, String>) config.get( "stateNames" ),
                (Map<String, String>) config.get( "stateDescriptions" ),
                (Map<String, String>) config.get( "transitionNames" ),
                (Map<String, String>) config.get( "transitionDescriptions" ),
                (Map<String, Map<String,Object>>) config.get( "transitionProperties" ),
                (String) config.get( "initialTransition" ),
                (Set<String>) config.get( "initialTransitions" ),
                (Map<String, Set<String>>) config.get( "validTransitions" ),
                (Map<String, String>) config.get( "defaultTransitions" ),
                (Map<String, String>) config.get( "targetStates" ),
                (TransitionSelector<S>) config.get( "transitionSelector" ),
                (Map<String, Class<Trigger<S,?>>>) config.get( "preTriggers" ),
                (Map<String, Class<Trigger<S,?>>>) config.get( "postTriggers" )
    	);
    }
    
    /**
     * Creates a state machine. Because the construction process is so
     * difficult, this is intended to be called only by the state machine
     * builder class.
     *
     * @param name
     *            name of the state machine
     * @param description
     *            description of the state machine
     * @param provider
     * @param logger
     *            shared logger
     * @param stateNames
     *            map of state key and display names, or labels
     * @param stateDescriptions
     *            map of state key and descriptions
     * @param transitionNames
     *            map of transition key and display names, or labels
     * @param transitionDescriptions
     *            map of transition key and descriptions
     * @param initialTransition
     *            key of default entry point into state machine
     * @param initialTransitions
     *            set of transition keys that correspond to entry points into
     *            state machine
     * @param validTransitions
     *            map of state keys and all allowed transitions from those
     *            states, if any
     * @param defaultTransitions
     *            map of state keys and the default transition for that state,
     *            if any
     * @param targetStates
     *            map of transition keys and target states for those transitions
     * @param preTriggers
     *            map of transition keys and pre-trigger operations
     * @param postTriggers
     *            map of transition keys and post-trigger operations
     * @throws MalformedStateMachineException if state machine is invalid
     *
     */
    StateMachine(final String name, final String description,
            final Map<String, String> stateNames,
            final Map<String, String> stateDescriptions,
            final Map<String, String> transitionNames,
            final Map<String, String> transitionDescriptions,
            final Map<String, Map<String,Object>> transitionProperties,
            final String initialTransition,
            final Set<String> initialTransitions,
            final Map<String, Set<String>> validTransitions,
            final Map<String, String> defaultTransitions,
            final Map<String, String> targetStates,
            final TransitionSelector<S> transitionSelector,
            final Map<String, Class<Trigger<S,?>>> preTriggers,
            final Map<String, Class<Trigger<S,?>>> postTriggers) throws MalformedStateMachineException {
        // set the name and description of the state machine
        super(name, description);

        // define the states in the state machine
        this.stateNames = Collections
                .unmodifiableMap(new HashMap<String, String>(stateNames));
        this.stateDescriptions = Collections
                .unmodifiableMap(new HashMap<String, String>(stateDescriptions));

        // define the transitions in the state machine
        this.transitionNames = Collections
                .unmodifiableMap(new HashMap<String, String>(transitionNames));
        this.transitionDescriptions = Collections
                .unmodifiableMap(new HashMap<String, String>(
                        transitionDescriptions));
        this.transitionProperties = Collections
                .unmodifiableMap(new HashMap<String, Map<String,Object>>(
                		transitionProperties));
        this.targetStates = Collections
                .unmodifiableMap(new HashMap<String, String>(targetStates));

        // define the entry points into the state machine
        this.initialTransition = initialTransition;
        this.initialTransitions = Collections
                .unmodifiableSet(new HashSet<String>(initialTransitions));

        // define the state machine transitions
        final Map<String, Set<String>> valid = new HashMap<String, Set<String>>(
                validTransitions.size());
        Set<String> transitions = null;
        for (final Entry<String, Set<String>> entry : validTransitions
                .entrySet()) {
            transitions = Collections.unmodifiableSet(new HashSet<String>(entry
                    .getValue()));
            valid.put(entry.getKey(), transitions);
        }
        this.validTransitions = Collections.unmodifiableMap(valid);
        this.defaultTransitions = Collections
                .unmodifiableMap(new HashMap<String, String>(defaultTransitions));

        // define the pluggable transition selection logic
        this.transitionSelector = transitionSelector == null ? new DefaultStateTransitionSelector<S>() : transitionSelector;

        // define the transition triggers
        this.preTriggerRegistry = Collections
                .unmodifiableMap(new HashMap<String, Class<Trigger<S,?>>>(
                        preTriggers));
        this.postTriggerRegistry = Collections
                .unmodifiableMap(new HashMap<String, Class<Trigger<S,?>>>(
                        postTriggers));

        // create trigger caches
        this.preTriggers = new HashMap<String, Trigger<S,?>>(
                preTriggerRegistry.size());
        this.postTriggers = new HashMap<String, Trigger<S,?>>(
                postTriggerRegistry.size());

        // try to initialize the cache of trigger instances
        loadTriggerCache(preTriggerRegistry, this.preTriggers);
        loadTriggerCache(postTriggerRegistry, this.postTriggers);

    }

    /**
     * @param registry
     * @param cache
     * @throws MalformedStateMachineException
     * @throws StateTransitionException
     */
    private void loadTriggerCache( final Map<String, Class<Trigger<S,?>>> registry, final Map<String, Trigger<S,?>> cache) throws MalformedStateMachineException {
        for (final Entry<String, Class<Trigger<S,?>>> trigger : registry.entrySet()) {
            cache.put( trigger.getKey(), newTrigger( trigger.getKey(), trigger.getValue() ) );
        }
    }

    /**
     * invoke the default initial transition defined by the state machine
     *
     * @param statefulObject
     *            the object to initialize
     * @throws StateTransitionException
     *             if the object could not be initialized
     */
    public final void initialize(final S statefulObject) throws StateTransitionException {
        invokeTransition(getInitialTransition(), statefulObject);
    }

    /**
     * test to see if item has been initialized
     *
     * @return <code>true</code> if state machine has been initialized,
     *         <code>false</code> otherwise.
     */
    final boolean isInitialized(final S statefulObject) {
        return statefulObject.getCurrentState() != null;
    }

    /**
     * @return
     */
    public final Map<String,Serializable> asGraph( Object... params ) {
    	Map<String,Serializable> graph = new LinkedHashMap<String,Serializable>();
		graph.put( "name", getName() );
		graph.put( "description", getDescription() );
		graph.put( "stateNames", (Serializable) getStateNames() );
        graph.put( "stateDescriptions", (Serializable) getStateDescriptions() );
        graph.put( "transitionNames", (Serializable) getTransitionNames() );
        graph.put( "transitionDescriptions", (Serializable) getTransitionDescriptions() );
        graph.put( "transitionProperties", new LinkedHashMap<>( transitionProperties ) );
        graph.put( "initialTransition", getInitialTransition() );
        graph.put( "initialTransitions", (Serializable) getInitialTransitions() );
        graph.put( "validTransitions", new LinkedHashMap<>( validTransitions ) );
        graph.put( "defaultTransitions", new LinkedHashMap<>( defaultTransitions ) );
        graph.put( "targetStates", new LinkedHashMap<>( targetStates ) );
        graph.put( "transitionSelector", transitionSelector.getClass().getName() );
        if( !preTriggerRegistry.isEmpty() ) {
        	graph.put( "preTriggers", (Serializable) getPreTriggers() );
        }
        if( !postTriggerRegistry.isEmpty() ) {
        	graph.put( "postTriggers", (Serializable) getPostTriggers() );
        }
        return graph;
    }

	/**
     * template method that defines the transition sequence.
     *
     * @param statefulObject
     *            the object to transition
     * @param transition
     *            the transition to invoke
     * @param transitionData
     * @throws StateTransitionException
     *             if the transition failed
     */
    @SuppressWarnings("unchecked")
	public final <OUT> OUT invokeTransition(final String transition, final S statefulObject, final Object... transitionData) throws StateTransitionException {
        // 1. check to make sure that a transition is provided
        if (isEmpty(transition)) {
            throw new IllegalArgumentException("must specify a transition");
        }

        // 2. check to make sure that a transition is provided
        if (statefulObject == null) {
            throw new IllegalArgumentException("must specify an object to transition");
        }

        // 3. find the appropriate transition key
        final String transitionKey = findTransitionKey(transition);

        if (transitionKey == null) {
            throw new StateTransitionException("no such transition: " + transition);
        }

        // 4. check the valid transitions for current state
        // 4a. get the current state of the object
        final String currentState = getObjectState(statefulObject);

        // 4b. auto advance until the appropriate pre transition step is reached
        Set<String> transitions = null;
        String stateText = null;
        if (currentState == null) {
            // not yet initialized
            transitions = getInitialTransitions();
            stateText = "a valid initial transition";
        } else {
            // active object at some state in the state machine
            transitions = validTransitions.get(currentState);
            stateText = String.format("allowed from state '%s'", getStateNames().get(currentState));
        }
        if (!transitions.contains(transitionKey)) {
            // if the requested transition is not allowed from the current
            // state, find path to get there using default transitions.
            final ArrayList<String> path = new ArrayList<String>();
            String t = null;
            String s = currentState;
            do {
                t = s == null ? getInitialTransition() : defaultTransitions.get(s);
                path.add(t);
                s = getTargetStates().get(t);
                transitions = validTransitions.get(s);
            } while ((transitions != null) && !transitions.contains(transitionKey));
            if (transitions == null) {
                // no path thru default transitions arrives at a state where the
                // requested transition is valid.
                throw new StateTransitionException(String.format( "transition '%s' is not %s", getTransitionNames().get(transitionKey), stateText));
            }

            // execute the transitions and walk object to the default state
            // where the requested transition can be executed
            OUT result = null;
            for (final String tr : path) {
            	result = invokeTransition(tr, statefulObject, transitionData);
                if( result != null ) {
                	return result;
                }
            }
        }

        // 6. perform pre-trigger to verify or validate transition
        // pre-requisites.
        if (preTriggers == null) {
            preTriggers = new HashMap<String, Trigger<S,?>>( preTriggerRegistry.size());
        }
        OUT result = this.<OUT>runTrigger(this.<OUT>getTrigger(preTriggerRegistry, preTriggers, transitionKey), transitionKey, statefulObject, transitionData);
        if( result != null ) {
        	return result;
        }

	    // 7. update the stateful object's state
	    statefulObject.setCurrentState(getTargetStates().get(transitionKey));

	    // 8. perform post-trigger to notify that transition occurred
        // since transition has occurred, ...
        if (postTriggers == null) {
            postTriggers = new HashMap<String, Trigger<S,?>>(postTriggerRegistry.size());
        }
        return (OUT) runTrigger(getTrigger(postTriggerRegistry, postTriggers, transitionKey), transitionKey, statefulObject, transitionData);

    }

    /**
     * utility method that returns the state key for the stateful object
     * @param statefulObject
     * @return
     */
    private String getObjectState(final S statefulObject) {
        final String state = statefulObject.getCurrentState();
        return findStateKey(state);
    }

	/**
	 * look up utility method that returns the state key for the given state name
	 * @param state
	 * @return
	 */
	private String findStateKey(String state) {
		String stateKey = null;
        if (!isEmpty(state)) {
            state = state.trim();
            for (final Entry<String, String> st : getStateNames().entrySet()) {
                if (st.getKey().equalsIgnoreCase(state)
                        || st.getValue().equalsIgnoreCase(state)) {
                    stateKey = st.getKey();
                    break;
                }
            }
        }

        return stateKey;
	}

    /**
     * look up utility method that returns the transition key for the given transition name
     * @param transition
     * @return
     */
    private String findTransitionKey(final String transition) {
        if (isEmpty(transition)) {
            return null;
        }

        String transitionKey = null;

        for (final Entry<String, String> tr : getTransitionNames().entrySet()) {
            if (tr.getKey().equalsIgnoreCase(transition.trim())
                    || tr.getValue().equalsIgnoreCase(transition.trim())) {
                transitionKey = tr.getKey();
                break;
            }
        }
        return transitionKey;
    }

    /**
     * @return
     */
    private final Map<String,String> getPreTriggers() {
    	return extractTriggerClassNames(preTriggerRegistry);
	}
    
    /**
     * @return
     */
    private final Map<String,String> getPostTriggers() {
    	return extractTriggerClassNames(postTriggerRegistry);
	}
   
	/**
	 * @param triggerRegistry
	 * @return
	 */
	private final Map<String, String> extractTriggerClassNames( Map<String, Class<Trigger<S, ?>>> triggerRegistry ) {
		Map<String,String> triggers = new LinkedHashMap<String,String>( triggerRegistry.size() );
    	for( Entry<String, Class<Trigger<S, ?>>> trigger : triggerRegistry.entrySet() ) {
    		triggers.put( trigger.getKey(), trigger.getValue().getName() );
    	}
		return triggers;
	}
	
    /**
     * utility method that returns an instance of the trigger for the specified transition
     * @param registry
     * @param cache
     * @param transition
     * @return
     * @throws StateTransitionException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private <OUT> Trigger<S,OUT> getTrigger(final Map<String, Class<Trigger<S,?>>> registry, final Map<String, Trigger<S,?>> cache, final String transition) throws StateTransitionException  {
        Trigger<S,OUT> trigger = (Trigger<S,OUT>) cache.get(transition);
        if (trigger == null) {
            final Class triggerClass = registry.get(transition);
            if (triggerClass != null) {
                try {
					trigger = (Trigger<S,OUT>) newTrigger(transition, triggerClass);
				} catch (final MalformedStateMachineException e) {
					throw new StateTransitionException( String.format("unable to load the trigger for transition '%s'.", transition), e );
				}
            }
        }
        return trigger;
    }

    /**
     * utility method that creates an instance of a trigger when state machine is constructed
     * @param transition
     * @param triggerClass
     * @param provider2
     * @param logger
     * @return
     * @throws StateTransitionException
     */
    private Trigger<S,?> newTrigger(final String transition, final Class<Trigger<S,?>> triggerClass) throws MalformedStateMachineException {
    	try {
    		return triggerClass.newInstance();
    	} catch (final Exception e) {
            throw new MalformedStateMachineException( String.format("unable to get trigger for transition '%s'.", getTransitionNames().get(transition)), e );
        }
    }

    /**
     * convenience method that executes pre and post triggers - if they are
     * defined for transition.
     *
     * @param transition
     *            the transition key
     * @param transitionData
     * @param triggerType
     *            the trigger collection that contains the trigger to execute
     *            for the key provided.
     * @return
     * @throws Exception
     *             if the trigger execution failed
     */
    private <OUT> OUT runTrigger(final Trigger<S,OUT> trigger, final String transition, final S obj, final Object... transitionData) throws StateTransitionException {
        // if a trigger has been defined for that transition
        if (trigger != null) {
            // run the trigger. if the transition is invalid, or not
            // authorized or permitted, raise a StateTransitionException.
            return trigger.onTransition( transition, obj, transitionData );
        }

    	return null;
    }

    /**
     * convenience method that advances the object thru its state machine by
     * using the default actions defined for this state machine.
     *
     * @param statefulObject
     *            the object to transition
     * @throws StateTransitionException
     *             if the transition failed
     */
    public final <OUT> OUT advance(final S statefulObject, final Object... transitionData) throws StateTransitionException {
        final String defaultTransition = getNextTransition(statefulObject);
        final Map<String,Serializable> validTransitions = new LinkedHashMap<String, Serializable>();
        Map<String,Object> transitionProperties = null;
        for( final String t : this.validTransitions.get( findStateKey( statefulObject.getCurrentState() ) ) ) {
        	transitionProperties = this.transitionProperties.get( t );
        	if( (transitionProperties == null) || transitionProperties.isEmpty() ) {
                validTransitions.put( t, "*" );
        	} else {
            	validTransitions.put( t, new LinkedHashMap<String, Object>( transitionProperties ) );
        	}
        }
        final String transition = transitionSelector.selectTransition(defaultTransition, validTransitions, statefulObject, transitionData );

        if (transition == null) {
            throw new StateTransitionException(
                    "no transition available for this state");
        }

        return invokeTransition(transition, statefulObject, transitionData);
    }

    /**
     * convenience method that helps advance the object through the state
     * machine by obtaining the next transition to execute based on the default
     * transition from the current state of the object.
     *
     * @param statefulObject
     *            the object to transition
     * @return the next transition to execute on the object
     */
    private String getNextTransition(final S statefulObject) {
        return statefulObject.getCurrentState() == null ? getInitialTransition()
                : defaultTransitions.get(statefulObject.getCurrentState());
    }

    /**
     * convenience method that helps determine if the object has reached a
     * terminal, or end state according to this state machine.
     *
     * @param statefulObject
     *            the object to test
     * @return <code>true</code> if the current state of the object has
     *         transitions defined, <code>false</code> otherwise.
     */
    final boolean isInEndState(final S statefulObject) {
        // make sure an object was provided
        if (statefulObject == null) {
            throw new IllegalArgumentException("must specify an object");
        }

        // if an object was provided, see if its current state is a terminal, or
        // end state
        return isEndState(statefulObject.getCurrentState());
    }

    /**
     * convenience method that helps determine if the state given is a terminal,
     * or end state according to this state machine.
     *
     * @param state
     *            the state to test
     * @return <code>true</code> if the state has transitions defined,
     *         <code>false</code> otherwise.
     */
    final boolean isEndState(final String state) {
        // make sure a state was provided
        if (isEmpty(state)) {
            throw new IllegalArgumentException("must specify a state");
        }

        // check to see that the state is a known and valid state
        boolean isValidState = false;

        for (final Entry<String, String> entry : getStateNames().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(state.trim())
                    || entry.getValue().equalsIgnoreCase(state.trim())) {
                isValidState = true;
                break;
            }
        }
        if (!isValidState) {
            throw new IllegalArgumentException(String.format(
                    "no such state: %s", state));
        }

        // if it is, to be an end state, there must be no transitions defined
        // for it.
        final Set<String> transitions = validTransitions.get(state.trim());
        return (transitions == null) || (transitions.size() == 0);
    }

//    /**
//     * load pre and post triggers
//     * @throws MalformedStateMachineException
//     */
//    public final void loadTriggers() throws MalformedStateMachineException {
//        if (preTriggers == null) {
//            preTriggers = new HashMap<String, Trigger<S,?>>(
//                    preTriggerRegistry.size());
//        }
//        loadTriggerCache(preTriggerRegistry, preTriggers);
//
//        if (postTriggers == null) {
//            postTriggers = new HashMap<String, Trigger<S,?>>(
//                    postTriggerRegistry.size());
//        }
//        loadTriggerCache(postTriggerRegistry, postTriggers);
//    }

    /**
     * Provide default for transient fields here
     *
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        preTriggers = null;
        postTriggers = null;
    }

    /**
     * Returns map of state names associated with this state machine
     *
     * @return map of state names
     */
    public final Map<String, String> getStateNames() {
        return new LinkedHashMap<>( stateNames );
    }

    /**
     * @return
     */
    final Map<String, String> getStateDescriptions() {
        return new LinkedHashMap<>( stateDescriptions );
    }

    /**
     * @return
     */
    final Map<String, String> getTransitionNames() {
        return new LinkedHashMap<>( transitionNames );
    }

    /**
     * @return
     */
    final Map<String, String> getTransitionDescriptions() {
        return new LinkedHashMap<>( transitionDescriptions );
    }

    /**
     * @return
     */
    final Map<String, Object> getTransitionProperties( final String transition ) {
    	return new LinkedHashMap<String,Object>( transitionProperties.get( findTransitionKey(transition) ) );
    }

    /**
     * @return
     */
    final Map<String, String> getTargetStates() {
        return new LinkedHashMap<>( targetStates );
    }

    /**
     * @return
     */
    final Set<String> getInitialTransitions() {
        return new LinkedHashSet<>( initialTransitions );
    }

    /**
     * @return
     */
    final String getInitialTransition() {
        return initialTransition;
    }

    /**
     * @return
     */
    public final String getInitialState() {
    	return stateNames.get( targetStates.get( initialTransition ) );
    }

	/**
	 * @param obj
	 * @return
	 */
	public final boolean isDone(final S obj) {
		return isInEndState( obj );
	}

}