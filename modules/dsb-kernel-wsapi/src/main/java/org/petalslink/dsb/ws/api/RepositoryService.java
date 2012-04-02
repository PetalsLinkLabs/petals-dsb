/**
 * 
 */
package org.petalslink.dsb.ws.api;

import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author chamerling
 * 
 */
@WebService
public interface RepositoryService {

    @WebMethod
    Set<String> getRepositories() throws DSBWebServiceException;

    @WebMethod
    Set<String> getRepositoryInformation(String name) throws DSBWebServiceException;

    @WebMethod
    void getRepositoryResources(String name) throws DSBWebServiceException;
}
