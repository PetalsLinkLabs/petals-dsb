/**
 * 
 */
package org.petalsling.dsb.integration.esmanagement;

import javax.xml.namespace.QName;

import org.petalslink.dsb.cxf.CXFHelper;

import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.WstopConstants;

import esstar.petalslink.com.service.management.admin._1_0.AdminManagement;
import junit.framework.TestCase;

/**
 * Test all the notification part
 * 
 * @author chamerling
 * 
 */
public class NotificationTest extends TestCase {

    public static final String baseURL = "http://localhost:7600/petals/ws";

    /**
     * @param name
     */
    public NotificationTest(String name) {
        super(name);
    }

    public void testGetResourceProperties() throws Exception {
        AdminManagement client = CXFHelper.getClient(baseURL, AdminManagement.class);
        com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.GetResourcePropertyResponse response = client
                .getResourceProperty(WstopConstants.TOPIC_SET_QNAME);
        
        assertNotNull(response);
        
        System.out.println("Response size :");
        //System.out.println(response.getAny());
    }

}
