/**
 * 
 */
package org.petalslink.dsb.kernel.ws.logger;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;

import org.objectweb.fractal.fraclet.annotation.annotations.FractalComponent;
import org.objectweb.fractal.fraclet.annotation.annotations.Interface;
import org.objectweb.fractal.fraclet.annotation.annotations.LifeCycle;
import org.objectweb.fractal.fraclet.annotation.annotations.Monolog;
import org.objectweb.fractal.fraclet.annotation.annotations.Provides;
import org.objectweb.fractal.fraclet.annotation.annotations.type.LifeCycleType;
import org.objectweb.util.monolog.api.Level;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.petals.util.oldies.LoggingUtil;
import org.petalslink.dsb.ws.api.logger.LoggerService;

/**
 * Monolog based logger management
 * 
 * @author chamerling
 * 
 */
@FractalComponent
@Provides(interfaces = { @Interface(name = "service", signature = LoggerService.class) })
public class LoggerServiceImpl implements LoggerService {

    @Monolog(name = "logger")
    private Logger logger;

    private LoggingUtil log;

    @LifeCycle(on = LifeCycleType.START)
    protected void start() {
        this.log = new LoggingUtil(this.logger);
        this.log.debug("Starting...");
    }

    @LifeCycle(on = LifeCycleType.STOP)
    protected void stop() {
        this.log.debug("Stopping...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.ws.api.logger.LoggerService#getAll()
     */
    @WebMethod
    public List<org.petalslink.dsb.ws.api.logger.Logger> getAll() {
        log.info("Get all loggers");
        List<org.petalslink.dsb.ws.api.logger.Logger> result = new ArrayList<org.petalslink.dsb.ws.api.logger.Logger>();
        Logger[] loggers = org.objectweb.util.monolog.Monolog.getMonologFactory().getLoggers();
        for (Logger logger : loggers) {
            org.petalslink.dsb.ws.api.logger.Logger l = new org.petalslink.dsb.ws.api.logger.Logger();
            l.setName(logger.getName());
            l.setLevel(logger.getCurrentLevel().getName());
            result.add(l);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petalslink.dsb.ws.api.logger.LoggerService#setLevel(org.petalslink
     * .dsb.ws.api.logger.Logger)
     */
    @WebMethod
    public boolean setLevel(org.petalslink.dsb.ws.api.logger.Logger logger) {
        log.info("Set logger level");

        boolean result = true;
        if (logger != null && logger.getName() != null && logger.getLevel() != null) {
            org.objectweb.util.monolog.Monolog
                    .getMonologFactory()
                    .getLogger(logger.getName())
                    .setLevel(
                            org.objectweb.util.monolog.Monolog.getMonologFactory().getLevel(
                                    logger.getLevel()));
        } else {
            result = false;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.ws.api.logger.LoggerService#getLevels()
     */
    @WebMethod
    public List<String> getLevels() {
        log.info("Get all logger levels");

        List<String> result = new ArrayList<String>();
        Level[] levels = org.objectweb.util.monolog.Monolog.getMonologFactory().getLevels();
        for (Level level : levels) {
            result.add(level.getName());
        }
        return result;
    }
}
