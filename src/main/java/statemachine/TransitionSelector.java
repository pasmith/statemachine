package statemachine;

import java.io.Serializable;
import java.util.Map;

/**
 * @author patrick
 *
 * @param <S>
 */
public interface TransitionSelector<S extends StatefulObject> {

	/**
	 * @param defaultTransition
	 * @param validTransitions
	 * @param obj
	 * @param transitionData
	 * @return
	 */
	String selectTransition( String defaultTransition, Map<String,Serializable> validTransitions, S obj, Object... transitionData );

}
