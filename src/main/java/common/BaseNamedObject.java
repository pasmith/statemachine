package common;

import static common.Utilities.isEmpty;
import static common.Assert.*;

import java.io.Serializable;

/**
 * top of hierarchy for named objects.
 *
 * @author patrick
 *
 */
public class BaseNamedObject implements NamedObject, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * the name of the named object
     */
    private String name;

    /**
     * a canonical form of the name
     */
    private String identifier;

    /**
     * the description for the named object
     */
    private final String description;

    /**
     * default constructor, initializes all object properties to null
     */
    public BaseNamedObject() {
        name = null;
        identifier = null;
        description = null;
    }

    /**
     * create an object with the specified name
     *
     * @param name
     *            the name for this object
     */
    public BaseNamedObject(final String name) {
        this( name, null );
    }

    /**
     * create an object with the specified name and description
     *
     * @param name
     *            the name for this object
     * @param description
     *            the description for the named object
     */
    public BaseNamedObject(final String name, final String description) {
        super();
        _assert( "name cannot be null or empty", !isEmpty( name ) );
        this.name = name.trim();
        identifier = toKey( name );
        this.description = isEmpty(description) ? null : description.trim();
    }

    /**
     * convenience method that computes a canonical form of the name
     *
     * @param name
     *            the name to process
     * @return the generated identifier
     */
    private String toKey(final String name) {
        final StringBuilder key = new StringBuilder();
        for( final char c : name.toCharArray() ) {
            if( Character.isJavaIdentifierStart(c) ) {
                key.append( Character.toLowerCase( c ) );
            }
        }
        return key.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override public boolean equals(final Object other) {
        if( other == this ) {
            return true;
        } else {
            if( other instanceof BaseNamedObject ) {
                return this.getClass().isAssignableFrom(other.getClass())   // other is a the same class, or a subclass of this object.
                                                                            // TODO this might violate the equals() contract - I think it has to be both ways
                    && identifier.equalsIgnoreCase( ((BaseNamedObject)other).identifier ) ;
            } else {
                return false;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override public int hashCode() {
        return identifier.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return String.format( "%s: %s", getClass().getName(), name );
    }

    /**
     * returns the pretty name for this object
     *
     * @return the object name
     */
    @Override
    public final String getName() {
        return name;
    }

    /**
     * returns the identifier that was derived from the name
     *
     * @return the identifier for this object
     */
    @Override
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * returns the description that was given to this object.
     *
     * @return the description of the object
     */
    public final String getDescription() {
        return description;
    }

}