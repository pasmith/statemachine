package common;

import java.io.Serializable;

/**
 * @author patrick
 *
 */
public interface NamedObject extends Serializable {

    /**
     * name provided
     * @return
     */
    String getName();

    /**
     * name normalized into a canonical form
     * @return
     */
    String getIdentifier();

}
