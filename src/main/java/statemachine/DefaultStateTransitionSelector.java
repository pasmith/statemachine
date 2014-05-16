package statemachine;

import java.io.Serializable;
import java.util.Map;

/**
 * @author patrick
 *
 */
public class DefaultStateTransitionSelector<S extends StatefulObject<S>> implements TransitionSelector<S> {

    /* (non-Javadoc)
     * @see statemachine.TransitionSelector#selectTransition(java.util.logging.Logger, java.lang.String, java.util.Map, java.lang.String, statemachine.StatefulObject, java.lang.Object[])
     */
    @Override public String selectTransition(final String defaultTransition, final Map<String, Serializable> validTransitions, final S obj, final Object... transitionData) {
        return defaultTransition;
    }

}
