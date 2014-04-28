package common;

import static common.Utilities.isEmpty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * helper class that defines counters that can be used to track impact of defensive programming.
 * 
 * @author patrick
 *
 */
public final class Assert {

	// executor
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	// convenient definition of action to increment anonymous counter
	private static final Runnable incrementGlobal = new Runnable() {			
		@Override public final void run() {
			// update global counter
			global.incrementAndGet();				
		}
	};

	// convenient definition of action to increment anonymous counter
	private static final Runnable incrementAnonymous = new Runnable() {			
		@Override public final void run() {
			// update anonymous counter
			anonymous.incrementAndGet();				
		}
	};

			
	// hide constructor
	private Assert(){}

	// create a counter that can be used to track any assert called.
	static final AtomicLong global = new AtomicLong();

	// create a counter that can be used to track anonymous assert calls only.
	static final AtomicLong anonymous = new AtomicLong();

	// add named counters
	static final Map<String,AtomicLong> counters = Collections.synchronizedMap( new HashMap<String,AtomicLong>() );

	
	/**
	 * @param comment
	 * @param value
	 */
	public static final void _assertTrue(String comment, boolean value) {
		try{
			_assert( value );
		} catch ( AssertionError e ) {
			throw new AssertionError( String.format( "%s. expected '%s', got '%s'.", comment, true, value), e );
		}
	}

	/**
	 * @param comment
	 * @param value
	 */
	public static final void _assertFalse(String comment, boolean value) {
		try {
			_assert( !value );
		} catch ( AssertionError e ) {
			throw new AssertionError( String.format( "%s. expected '%s', got '%s'.", comment, false, value), e );
		}
	}

	/**
	 * @param comment
	 * @param o
	 */
	public static final void _assertNotNull(String comment, Object o) {
		try {
			_assert( o != null );
		} catch ( AssertionError e ) {
			throw new AssertionError( String.format( "%s. expected '%s' to not be '%s'.", comment, o, null), e );
		}		
	}

	/**
	 * @param comment
	 * @param o
	 */
	public static void _assertNull(String comment, Object o) {
		try {
			_assert( o == null );
		} catch ( AssertionError e ) {
			throw new AssertionError( String.format( "%s. expected '%s', got '%s'.", comment, null, o), e );
		}		
	}
	
	/**
	 * @param comment
	 * @param o1
	 * @param o2
	 */
	public static final void _assertEquals(String comment, Object o1, Object o2) {
		try {
			_assert( o1 == null ? o2 == null : o2 != null && o1.equals( o2 ) );
		} catch ( AssertionError e ) {
			throw new AssertionError( String.format( "%s. expected '%s', got '%s'.", comment, o1, o2), e );
		}
	}
	
	/**
	 * @param comment
	 * @param o1
	 * @param o2
	 */
	public static final void _assertNotSame(String comment, Object o1, Object o2) {
		try{
			_assert( o1 == null ? o2 != null : o2 == null || !o1.equals( o2 ) );
		} catch ( AssertionError e ) {
			throw new AssertionError( String.format( "%s. expected '%s' to not be '%s'.", comment, o1, o2), e );
		}
	}
	
	/**
	 * @param comment
	 * @param x
	 * @param y
	 * @param tolerance
	 */
	public static final void _assertEquals(String comment, double x, double y, double tolerance) {
		try {
			_assert( Math.abs( x - y ) <= tolerance );
		} catch ( AssertionError e ) {
			throw new AssertionError( String.format( "%s. expected '%s', got '%s'.", comment, x, y), e );
		}
	}
	
	
	
	/**
	 * anonymous wrapper around 
	 * @param condition
	 */
	public static void _assert( boolean condition ){
		// TODO get use case name from stack trace?		
		// increment global counters in background thread
		evaluate( incrementAnonymous, condition);
	}

	/**
	 * evaluate the condition
	 * @param incrementanonymous2 
	 * @param condition
	 */
	private static void evaluate(Runnable incrementCounter, boolean condition) {
		// increment the counter specified and the global counter
		executor.execute( incrementGlobal );
		executor.execute( incrementCounter );
		
		// evaluate the assertion
		assert condition;
	}
	
	/**
	 * named counter
	 * 
	 * @param name counter name
	 * @param condition
	 */
	public static void _assert(String name, boolean condition) {
		// determine which counter to increment and evaluate the condition
		evaluate( isEmpty(name) ? incrementAnonymous : getIncrementCounterAction( name ), condition);
	}

	/**
	 * create an increment action for the counter associated with the given counter name.
	 * @param name of counter
	 * @return action to increment the specified counter
	 */
	private static Runnable getIncrementCounterAction(String name) {
		assert !isEmpty( name );
		AtomicLong counter = counters.get( name );
		if( counter == null ) {
			counter = new AtomicLong();
			counters.put( name, counter );
		}
		final AtomicLong c = counter;
		return new Runnable() {			
			@Override public void run() {
				c.incrementAndGet();
			}
		};
	}

	/**
	 * convenience method to reset counters. reserved for internal use only.
	 */
	static final void reset() {
		global.set( 0l );
		anonymous.set( 0l );
		counters.clear();		
	}
}
