/**
 * 
 */
package org.petalslink.dsb.servicepoller.api;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

/**
 * Standard API definition which hides the data handler handling and just
 * provide a DOM based API.
 * 
 * @author chamerling
 * 
 */
public interface WSNPoller {

    /**
     * Start polling the given service with the given information.
     * 
     * @param toPoll
     *            service to poll
     * @param inputMessage
     *            input message to send to the {@link #toPoll()} service
     * @param replyTo
     *            send the response from the polled service to this one (if
     *            any)...
     * @throws ServicePollerException
     */
    String start(ServicePollerInformation toPoll, Document inputMessage, String cronExpression,
            ServicePollerInformation replyTo, QName topic) throws ServicePollerException;
    
    /**
     * 
     * @return
     */
    List<WSNPollerServiceInformation> getInformation();

    /**
     * 
     * @param id
     * @return
     * @throws ServicePollerException
     */
    boolean stop(String id) throws ServicePollerException;

    /**
     * 
     * @param id
     * @return
     * @throws ServicePollerException
     */
    boolean pause(String id) throws ServicePollerException;

    /**
     * 
     * @param id
     * @return
     * @throws ServicePollerException
     */
    boolean resume(String id) throws ServicePollerException;

}
