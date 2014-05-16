package issues;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import statemachine.StateMachine;
import statemachine.StateMachineBuilder;
import test.BaseTestCase;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author patrick
 *
 */
public class IssueTrackerTest extends BaseTestCase<StateMachine<ChangeRequest>, ChangeRequest> {

    /* (non-Javadoc)
     * @see test.BaseTestCase#getPerfTestResultMessage()
     */
    @Override protected final String getPerfTestResultMessage() {
        return createPerformanceTestResultMessage( "executed", "state transitions" );
    }

    /* (non-Javadoc)
     * @see test.BaseTestCase#getThreadSafetyResultMessage()
     */
    @Override protected final String getThreadSafetyResultMessage() {
        return createThreadSafetyTestResultMessage( "executed", "state transitions" );
    }

    /* (non-Javadoc)
     * @see test.BaseTestCase#generateTestData(java.util.Map)
     */
    @Override protected final ChangeRequest generateTestData (final Map<String, Object> params) throws Exception {
        return new ChangeRequest( "bug-1" );
    }

    /* (non-Javadoc)
     * @see test.BaseTestCase#getComponentUnderTest()
     */
    @SuppressWarnings("unchecked")
    @Override protected final StateMachine<ChangeRequest> getComponentUnderTest() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure( JsonParser.Feature.ALLOW_COMMENTS, true );
        final Map<String,Serializable> config = mapper.readValue( String.class.getResourceAsStream( "/issueTracker.json" ) , LinkedHashMap.class );
        return new StateMachineBuilder<ChangeRequest, StateMachine<ChangeRequest>>().fromGraph( config ).create();
    }

    /* (non-Javadoc)
     * @see test.BaseTestCase#verifyFunctionality(java.util.Map, java.lang.Object, java.lang.Object, java.util.concurrent.atomic.AtomicInteger)
     */
    @Override final protected void verifyFunctionality(final Map<String, Object> params, final StateMachine<ChangeRequest> stateMachine, final ChangeRequest changeRequest, final AtomicInteger countStateTransitions) throws Exception {
        assertNull( "change requests should not have a state assigned to them until they are submitted.", changeRequest.getCurrentState() );

        // submit the change request
        stateMachine.initialize( changeRequest );
        assertEquals( "change requests should be in the 'submitted' state after they are received by the system.", "submitted", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

        // open the ticket
        stateMachine.advance( changeRequest );
        assertEquals( "change requests should be in the 'opened' state after they are advanced by the system.", "opened", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

        // open the ticket
        stateMachine.invokeTransition( "resubmit", changeRequest);
        assertEquals( "change requests should be in the 'submitted' state after they are skipped or expired.", "submitted", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

        // open the ticket (again)
        stateMachine.advance( changeRequest );
        assertEquals( "change requests should be in the 'opened' state after they are advanced by the system.", "opened", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

        // resolve the ticket
        stateMachine.advance( changeRequest );
        assertEquals( "change requests should be in the 'resolved' state after they are advanced by the system.", "resolved", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

        // reject the ticket (again)
        stateMachine.invokeTransition( "reject", changeRequest );
        assertEquals( "change requests should be in the 'opened' state after they have been rejected.", "opened", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

        // resolve the ticket - again
        stateMachine.advance( changeRequest );
        assertEquals( "change requests should be in the 'resolved' state after they are advanced by the system.", "resolved", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

        // close the ticket
        stateMachine.advance( changeRequest );
        assertEquals( "change requests should be in the 'closed' state after they are advanced by the system.", "closed", changeRequest.getCurrentState() );
        countStateTransitions.incrementAndGet();

    }

}
