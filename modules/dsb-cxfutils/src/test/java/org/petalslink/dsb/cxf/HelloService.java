/**
 * 
 */
package org.petalslink.dsb.cxf;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author chamerling
 *
 */
@WebService
public interface HelloService {

    @WebMethod
    String sayHello(String in);
}
