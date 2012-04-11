/**
 * 
 */
package org.petalslink.dsb.ws.api;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Test if the current DSB node is starting, started, stopping, etc...
 * 
 * @author chamerling
 * 
 */
@WebService
public interface StatusService {
    
    @WebMethod
    boolean isStarting();
    
    @WebMethod
    boolean isStarted();
    
    @WebMethod
    boolean isStopping();
    
    @WebMethod
    long getStartedAt();
    
}
