/**
 * 
 */
package org.petalslink.dsb.ws.api;

import java.util.List;

import javax.jws.WebService;

/**
 * This is just a shortcut to the WSN specification. Up to the DSB to use the
 * needed implementation.
 * 
 * @author chamerling
 * 
 */
@WebService
public interface WSNManagementService {

    /**
     * Get all the available topics
     * 
     * @return
     */
    List<String> getAvailableTopics();

    /**
     * Get all the subscribers for the given topic. Get all if given topic is
     * null.
     * 
     * @param topic
     * @return
     */
    List<String> getSubscribers(String topic);
}
