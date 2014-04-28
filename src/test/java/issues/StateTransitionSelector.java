package issues;

import java.io.Serializable;
import java.util.Map;

import statemachine.TransitionSelector;
import test.TestFixture;

/**
 * @author patrick
 *
 */
public class StateTransitionSelector implements TransitionSelector<ChangeRequest>, TestFixture {

    /* (non-Javadoc)
     * @see statemachine.TransitionSelector#selectTransition(java.util.logging.Logger, java.lang.String, java.util.Map, java.lang.String, statemachine.StatefulObject, java.lang.Object[])
     */
    @Override public String selectTransition(final String defaultTransition, final Map<String, Serializable> validTransitions, final ChangeRequest obj, final Object... transitionData) {
        return defaultTransition;
    }

}
