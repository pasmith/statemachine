package common;


/**
 * @author patrick
 *
 * @param <B>
 * @param <V>
 */
public interface Builder<B extends Builder<?,V>,V> {
	
	/**
	 * @return
	 * @throws Exception
	 */
	public abstract V create() throws Exception;
	
}
