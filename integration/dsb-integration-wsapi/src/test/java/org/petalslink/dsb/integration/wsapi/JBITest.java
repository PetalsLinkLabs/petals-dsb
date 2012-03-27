/**
 * 
 */
package org.petalslink.dsb.integration.wsapi;

import junit.framework.TestCase;

import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.ws.api.jbi.ComponentInformationService;

/**
 * @author chamerling
 *
 */
public class JBITest extends TestCase {
    
    static String URL = "http://localhost:7600/petals/ws/";
    
    public void testGetComponents() throws Exception {
        // we have at least one component here...
        ComponentInformationService info = CXFHelper.getClient(URL, ComponentInformationService.class);
        System.out.println(info.getComponentNames());
    }

}
