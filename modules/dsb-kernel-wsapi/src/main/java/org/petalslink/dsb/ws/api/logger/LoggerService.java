/**
 * 
 */
package org.petalslink.dsb.ws.api.logger;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author chamerling
 * 
 */
@WebService
public interface LoggerService {

    /**
     * Get all the container loggers
     * 
     * @return
     */
    @WebMethod
    List<Logger> getAll();

    /**
     * Update a logger level by pushing a new {@link Logger} instance. Does
     * nothing if logger with the given name is not available.
     * 
     * @param logger
     * @return true if success, false in all other cases
     */
    @WebMethod
    boolean setLevel(Logger logger);

    /**
     * Get all the available levels
     * 
     * @return
     */
    @WebMethod
    List<String> getLevels();
}
