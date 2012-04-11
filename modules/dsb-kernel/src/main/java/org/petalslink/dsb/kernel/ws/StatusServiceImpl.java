/**
 * 
 */
package org.petalslink.dsb.kernel.ws;

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
import org.petalslink.dsb.ws.api.StatusService;

/**
 * @author chamerling
 * 
 */
@FractalComponent
@Provides(interfaces = { @Interface(name = "service", signature = StatusService.class) })
public class StatusServiceImpl implements StatusService {

    @Monolog(name = "logger")
    private Logger logger;

    private LoggingUtil log;

    private boolean starting;

    private boolean started;

    private boolean stopping;
    
    private long startedAt = -1L;

    @LifeCycle(on = LifeCycleType.START)
    protected void start() {
        this.log = new LoggingUtil(this.logger);
        starting = true;

    }

    @LifeCycle(on = LifeCycleType.STOP)
    protected void stop() {
        started = false;
        stopping = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.ws.api.StatusService#isStarting()
     */
    public boolean isStarting() {
        return starting;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.ws.api.StatusService#isStarted()
     */
    public boolean isStarted() {
        return started;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.ws.api.StatusService#isStopping()
     */
    public boolean isStopping() {
        return stopping;
    }
    
    /*
     * (non-Javadoc)
     * @see org.petalslink.dsb.ws.api.StatusService#getStartedAt()
     */
    public long getStartedAt() {
        return this.startedAt;
    }

    /**
     * Let's change the flag with the help of the lifecycle listener...
     */
    @LifeCycleListener(phase = Phase.START, priority = 0)
    public void started() {
        starting = false;
        started = true;
        stopping = false;
        startedAt = System.currentTimeMillis();
    }

    /**
     * High priority means that this component is called first when the
     * container starts to stop...
     */
    @LifeCycleListener(phase = Phase.STOP, priority = 10000)
    public void stopping() {
        starting = false;
        started = false;
        stopping = true;
    }
}
