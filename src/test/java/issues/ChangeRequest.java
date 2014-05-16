package issues;

import statemachine.StatefulObject;
import test.TestFixture;

/**
 * @author patrick
 *
 */
public class ChangeRequest extends StatefulObject<ChangeRequest> implements TestFixture {
	private static final long serialVersionUID = 1L;


	ChangeRequest(final String name) {
		super(name);
	}

	ChangeRequest(final String name, final String currentState) {
		super(name, currentState);
	}


}
