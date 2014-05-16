package statemachine;

/**
 * An action that the state machine can trigger before, during, are after a
 * transition.
 *
 * <code>pre</code> triggers are used to validate a transition, or check
 * permissions. If something is not valid, the trigger can fail the
 * transition, which means that the transition does not occur, and the state
 * remains unchanged.
 *
 * <code>post</code> triggers are used after a transition has occurred to
 * chain other things, or initialize, or populate fields from the transition
 * data.
 *
 * @author patrick
 *
 */
public interface Trigger<S extends StatefulObject<?>, OUT> {
    /**
     * Call back that can be implemented to perform an action when a
     * transition occurs.
     *
     * @param transition
     *            the transition being invoked
     * @param obj
     *            the object going through the state transition
     * @param transitionData the data needed by the trigger
     *
     * @return <code>null</code> it pre-trigger should allow transition, otherwise value indicates reason(s) why transition is not allowed. ignored by post-trigger.
     *
     * @throws StateTransitionException
     *             if errors occurred while trying to execute the trigger.
     */
    OUT onTransition(String transition, S obj, Object... transitionData ) throws StateTransitionException;
}