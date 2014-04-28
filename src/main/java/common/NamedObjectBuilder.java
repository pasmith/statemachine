package common;

import static common.Utilities.isEmpty;


/**
 * Builder for named objects. this is not needed to build named objects per se,
 * but it is useful for builders of classes that extend NamedObjects.
 *
 * @author patrick
 *
 * @param <O>
 *            the object type to build
 * @param <B>
 *            the builder type to return
 */
public class NamedObjectBuilder<B extends NamedObjectBuilder<?,O>, O extends NamedObject> implements Builder<B,O> {
    /**
     * the name for the named object to build
     */
    private String name = null;

    /**
     * the description for the named object to build
     */
    private String description = null;

    /**
     * set the name for the object to build
     *
     * @param name
     *            the name for the object
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    public final B withName (final String name) {
        if( isEmpty( name ) ) {
            throw new IllegalArgumentException( "name cannot be empty" );
        }
        this.name = name.trim();
        return (B) this;
    }

    /**
     * set the description for the object to build
     *
     * @param description
     *            the description for the object
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    public final B withDescription (final String description) {
        this.description = isEmpty( description ) ? null : description.trim();
        return (B) this;
    }

    /**
     * check required fields
     */
    protected void validate() {
        if( name == null ) {
            throw new IllegalStateException( "named object must have a name" );
        }
    }

    /* (non-Javadoc)
     * @see common.Builder#create()
     */
    @SuppressWarnings("unchecked")
	@Override public O create() throws Exception {
        final Class<? extends NamedObject> type = getType();
        try {
            return (O) type.getDeclaredConstructor( String.class, String.class ).newInstance( name, description );
        } catch (final Throwable e) {
            throw new IllegalArgumentException( "invalid named object type: " + type.getName(), e );
        }
    }

    /**
     * override this class to change the type returned by this builder
     *
     * @return the type to use for this class
     */
    @SuppressWarnings("unchecked")
    protected Class<O> getType() {
        return (Class<O>) BaseNamedObject.class;
    }

    /**
     * get description associated
     *
     * @return description
     */
	public String getDescription() {
		return description;
	}

	/**
	 * get name associated
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}
}
