package statemachine;

import java.util.Arrays;

/**
 * Indicate that a state transition failed.
 * @author patrick
 *
 */
public class StateTransitionException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private Throwable[] causes;

    /**
     * Create state transition exception to indicate that there was a problem was with a transition.
     */
    public StateTransitionException() {
        super();
        causes = null;
    }

    /**
     * Create state transition exception with a reason describing what the
     * problem was with the transition.
     *
     * @param reason
     *            a description of the problem with the transition
     */
    public StateTransitionException(final String reason) {
        super(reason);
        causes = null;
    }

    /**
     * Create state transition exception describing the cause of the problem
     * with the transition.
     *
     * @param cause
     *            the cause of the transition problem
     */
    public StateTransitionException(final Throwable cause) {
        super(cause);
        causes = new Throwable[]{cause};
    }

    /**
     * Create state transition exception with a reason and cause describing what
     * the problem was with the transition.
     *
     * @param reason
     *            a description of the problem with the transition
     * @param cause
     *            the cause of the transition problem
     */
    public StateTransitionException(final String reason, final Throwable cause) {
        super(reason, cause);
        causes = new Throwable[]{cause};
    }

    /**
     * @param reason
     * @param causes
     */
    public StateTransitionException(final String reason, final Throwable[] causes) {
        this(reason, causes != null && causes.length > 0 ? causes[0] : null );
        if( causes != null && causes.length > 1 ) {
            this.causes = Arrays.copyOf( causes, causes.length );
        }
    }

    /**
     * get causes for the exception for more information
     * @return causes of exception
     */
    public final Throwable[] getCauses() {
        return Arrays.copyOf( causes, causes.length );
    }
}