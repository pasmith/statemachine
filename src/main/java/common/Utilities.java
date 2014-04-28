package common;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;


/**
 * Basic utilities for the project.
 *
 * @author patrick
 *
 */
public final class Utilities {

    /**
     * non alpha numeric character set using predefined java regexp character
     * sets:
     *
     * \w A word character: [a-zA-Z_0-9] \W A non-word character: [^\w]
     *
     * note: this will allow the _ character, but we use this patternd to
     * validate things like project name and account name. so this should be ok.
     *
     * we should require more than 1 character minimum. is 2 enough? {2,}
     *
     * in this case it is not necessary to use ^ or $ since we will be trimming
     * the input to validate and only be allowing word characters.
     */
    public final static Pattern NON_ALPHA_NUMERIC_PATTERN = Pattern.compile("\\W+");

	/**
	 * pattern for UUID
	 */
	public static final String UUID_PATTERN = "[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}";


    /**
     * can't construct. use methods as static imports.
     */
    private Utilities(){}

    /**
     * efficient way to see if string is empty.
     *
     * @param value string to check
     * @return <code>true</code> is empty, <code>false</code> otherwise.
     */
    public static boolean isEmpty( final String value ) {
        // return true fast if value is null;
        if( value == null ) {
            return true;
        }

        // fail fast if there is at least 1 non-whitespace character
        for( int i=0;i<value.length();i++ ) {
            if( Character.isWhitespace( value.charAt(i) ) ) {
                // move on to next character
                continue;
            } else {
                // we found a non-whitespace character. stop processing.
                return false;
            }
        }

        // either string was empty or it only had whitespace characters
        return true;
    }

    /**
     * @param value
     * @return
     */
    public static boolean isEmpty(final Serializable value) {
        return (value == null) ||
           ((value instanceof String) && isEmpty((String)value)) ||
           ((value instanceof Collection) && ((Collection<?>)value).isEmpty()) ||
           ((value instanceof Map) && ((Map<?,?>)value).isEmpty()) ||
           (value.getClass().isArray() && (Array.getLength( value ) == 0));
    }

    /**
     * Verifies if passed string is alphanumeric
     * @param value string passed
     * @return true if passed parameter is alphanumeric, false otherwise
     */
	public static boolean checkAlphanumericValue(final String value) {
	    // no need to instantiate a matcher
		if(NON_ALPHA_NUMERIC_PATTERN.matcher(value).find()) {
			return false;
		}
		return true;
	}

	/**
	 * utility method to get stack trace from an exception as a string
	 *
	 * @param aThrowable exception thrown
	 * @return stack trace as string
	 */
	public static String getStackTrace(final Throwable aThrowable) {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	}

	/**
	 * @param graph
	 */
	@SuppressWarnings("unchecked")
	public static void prune( final Map<String, Serializable> graph ) {
		final Iterator<Entry<String, Serializable>> entries = graph.entrySet().iterator();
		Entry<String, Serializable> entry = null;
		while( entries.hasNext() ) {
			entry = entries.next();
			if( (entry.getValue() == null) || ((entry.getValue() instanceof String) && isEmpty( (String) entry.getValue())) ) {
				entries.remove();
			} else if( entry.getValue() instanceof Map<?,?> ) {
				prune( (HashMap<String, Serializable>) entry.getValue() );
				if( ((Map<?,?>)entry.getValue()).size() == 0 ) {
					entries.remove();
				}
			} else if( (entry.getValue() instanceof List<?>) && (((List<?>) entry.getValue()).size() == 0) ) {
				entries.remove();
			} else if( entry.getValue().getClass().isArray() && (Array.getLength( entry.getValue() ) == 0) ) {
				entries.remove();
			}
		}
	}

    /**
     * utility method to delete a folder and all its contents.
     *
     * @param file
     * @return
     */
    public static final boolean delete(final File file) {
        boolean deleted = true;
        if( file.isDirectory() ) {
            for( final File f : file.listFiles() ) {
                deleted &= delete(f);
            }
        }
        return deleted & file.delete();
    }

}
