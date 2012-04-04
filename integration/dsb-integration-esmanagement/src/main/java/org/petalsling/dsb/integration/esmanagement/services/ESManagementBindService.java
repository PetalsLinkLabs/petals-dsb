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
public interface ESManagementBindService {

    @WebMethod
    String foo(String bar);
}
