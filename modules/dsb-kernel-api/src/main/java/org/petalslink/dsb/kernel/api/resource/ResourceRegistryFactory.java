/**
 * 
 */
package org.petalslink.dsb.kernel.api.resource;

/**
 * A registry cache. The instance is built by the kernel at startup...
 * 
 * @author chamerling
 *
 */
public class ResourceRegistryFactory {
    
    private static ResourceRegistry instance;
    
    public static ResourceRegistry get() {
        if (instance == null) {
            // can we lake it...
        } else {
            return instance;
        }
        
        return instance;
    }
    
    public static final void set(ResourceRegistry registry) {
        if (registry == null) {
            return;
        }
        
        instance = registry;
    }

}
