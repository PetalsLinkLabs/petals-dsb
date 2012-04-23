/**
 * 
 */
package org.petalslink.dsb.integration.bsm.mocks;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * This service is used for binding and invocation to check if the service is
 * called and if the BSM layer gets notified when message are echanged between
 * the service and the client.
 * 
 * @author chamerling
 * 
 */
@WebService
public interface BSMIntegrationService {

    @WebMethod
    String sayHello();

}
