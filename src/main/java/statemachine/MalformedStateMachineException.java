package statemachine;

/**
 * @author patrick
 *
 */
public class MalformedStateMachineException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MalformedStateMachineException() {
	}

	/**
	 * @param reason
	 */
	public MalformedStateMachineException(String reason) {
		super(reason);
	}

	/**
	 * @param cause
	 */
	public MalformedStateMachineException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param reason
	 * @param cause
	 */
	public MalformedStateMachineException(String reason, Throwable cause) {
		super(reason, cause);
	}

}
