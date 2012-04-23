/**
 * 
 */
package org.petalslink.dsb.kernel.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.fraclet.annotation.annotations.FractalComponent;
import org.objectweb.fractal.fraclet.annotation.annotations.Interface;
import org.objectweb.fractal.fraclet.annotation.annotations.LifeCycle;
import org.objectweb.fractal.fraclet.annotation.annotations.Monolog;
import org.objectweb.fractal.fraclet.annotation.annotations.Provides;
import org.objectweb.fractal.fraclet.annotation.annotations.type.LifeCycleType;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.petals.util.oldies.LoggingUtil;
import org.petalslink.dsb.annotations.LifeCycleListener;
import org.petalslink.dsb.annotations.Phase;
import org.petalslink.dsb.kernel.api.resource.Resource;
import org.petalslink.dsb.kernel.api.resource.ResourceRegistry;
import org.petalslink.dsb.kernel.api.resource.ResourceRegistryFactory;

/**
 * A resource registry. This may be helpful to cache things and to retrieve them
 * quickly. Please consider to use the static factory instead of a fractal link
 * for better configuration purposes at
 * org.petalslink.dsb.kernel.api.resource.ResourceRegistryFactory.
 * 
 * @author chamerling
 * 
 */
@FractalComponent
@Provides(interfaces = { @Interface(name = "service", signature = ResourceRegistry.class) })
public class ResourceRegistryImpl implements ResourceRegistry {

    @Monolog(name = "logger")
    private Logger logger;

    private LoggingUtil log;

    /**
     * Key is the resource type as string, value is a set of resources.
     */
    private Map<String, Set<Resource>> cache;

    @LifeCycle(on = LifeCycleType.START)
    protected void start() {
        this.log = new LoggingUtil(this.logger);
        this.log.debug("Starting...");
        this.cache = new HashMap<String, Set<Resource>>();
    }

    @LifeCycle(on = LifeCycleType.STOP)
    protected void stop() {
        this.log.debug("Stopping...");
    }

    @LifeCycleListener(phase = Phase.START, priority = 100000)
    public void init() {
        ResourceRegistryFactory.set(this);
    }

    public void put(Resource... resource) {
        for (Resource r : resource) {
            if (r.getType() != null) {
                if (!cache.containsKey(r.getType())) {
                    cache.put(r.getType(), new java.util.HashSet<Resource>());
                }
                cache.get(r.getType()).add(r);
            }
        }
    }

    public List<Resource> all() {
        List<Resource> result = new ArrayList<Resource>();
        for (String key : cache.keySet()) {
            for (Resource resource : cache.get(key)) {
                result.add(resource);
            }
        }
        return result;
    }

    public List<Resource> get(String type) {
        if (type == null) {
            return new ArrayList<Resource>();
        }
        return new ArrayList<Resource>(cache.get(type));
    }
}
