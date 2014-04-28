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
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public MalformedStateMachineException(String reason) {
		super(reason);
	}

	/**
	 * @param arg0
	 */
	public MalformedStateMachineException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MalformedStateMachineException(String reason, Throwable cause) {
		super(reason, cause);
	}

}
