package statemachine;

import java.util.Date;

import common.BaseNamedObject;



/**
 * a named object whose life cycle is managed by a state machine
 *
 * @author patrick
 *
 */
public class StatefulObject<S extends StatefulObject<?>> extends BaseNamedObject<S> {
    private static final long serialVersionUID = 1L;

    /**
     * the current state of the object
     */
    private String state;

    /**
     * indicates when the last state transition occurred
     */
    private long updatedOn;


    /**
     * @param name
     */
    protected StatefulObject(final String name) {
        super( name );
        state = null;
        updatedOn = System.currentTimeMillis();
    }

    /**
     * @param currentState
     */
    protected StatefulObject(final String name, final String currentState) {
        super( name );
        this.state = currentState;
        updatedOn = System.currentTimeMillis();
    }

    /**
     * gets the current state for the object
     *
     * @return the current state
     */
    public final String getCurrentState() {
        return state;
    }

    /**
     * sets the current state for the object. should only be called by the state
     * machine that manages this object.
     *
     * @param state
     *            the new state
     */
    final void setCurrentState(final String state) {
        this.state = state;
        updatedOn = System.currentTimeMillis();
    }

    /**
     * @return when the last state transition occurred
     */
    protected final Date getLastUpdatedOn() {
    	return new Date(updatedOn);
    }

}
