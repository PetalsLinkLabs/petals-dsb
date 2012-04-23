/**
 * 
 */
package org.petalslink.dsb.kernel.api.resource;

import java.util.List;

/**
 * A kernel resource registry. Used to store references to all the important
 * resources of the local kernel.
 * 
 * @author chamerling
 * 
 */
public interface ResourceRegistry {
    
    void put(Resource... resource);
    
    List<Resource> all();
    
    List<Resource> get(String type);

}
