/**
 * 
 */
package org.petalsling.dsb.integration.esmanagement.services;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author chamerling
 *
 */
@WebService
public interface ESManagementProxifyService {

    @WebMethod
    String foo(String bar);
}
