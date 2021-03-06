/**
 * 
 */
package org.petalslink.dsb.servicepoller.api;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @author chamerling
 * 
 */
@WebService
public interface WSNPollerService {

    @WebMethod
    String start(@WebParam(name = "toPoll") ServicePollerInformation toPoll,
            @WebParam(name = "inputMessage") DocumentHandler inputMessage,
            @WebParam(name = "cronExpression") String cronExpression,
            @WebParam(name = "sendReplyTo") ServicePollerInformation replyTo,
            @WebParam(name = "topicName") String topicName,
            @WebParam(name = "topicURI") String topicURI,
            @WebParam(name = "topicPrefix") String topicPrefix) throws ServicePollerException;
    
    @WebMethod
    List<WSNPollerServiceInformation> getInformation();
    
    /**
     * 
     * @param id
     * @return
     * @throws ServicePollerException
     */
    @WebMethod
    boolean stop(String id) throws ServicePollerException;

    /**
     * 
     * @param id
     * @return
     * @throws ServicePollerException
     */
    @WebMethod
    boolean pause(String id) throws ServicePollerException;

    /**
     * 
     * @param id
     * @return
     * @throws ServicePollerException
     */
    @WebMethod
    boolean resume(String id) throws ServicePollerException;

}
