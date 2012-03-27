/**
 * 
 */
package org.petalslink.dsb.integration.wsapi;

import junit.framework.TestCase;

import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.ws.api.HelloService;

import static org.petalslink.dsb.integration.wsapi.Logs.INFO;
import static org.petalslink.dsb.integration.wsapi.Logs.KO;
import static org.petalslink.dsb.integration.wsapi.Logs.OK;

/**
 * Business services test
 * 
 * @author chamerling
 *
 */
public class BusinessWSTest extends TestCase {
    
    static String URL = "http://localhost:7600/petals/ws/";
    
    @Override
    protected void setUp() throws Exception {
        INFO("Starting DSB node");
    }
    
    @Override
    protected void tearDown() throws Exception {
        INFO("Stopping DSB node");
    }
    
    public void testCallKernel() throws Exception {
        INFO("Calling the kernel hello service exposed by the DSB");
        HelloService client = CXFHelper.getClient(URL, HelloService.class);
        String input = "Hello";
        String result = client.sayHello(input);
        
        INFO("Service returned '%s'", result);
        assertTrue(result.contains(input));
        OK("Valid response");
    }
    
    public void testCallKernelServiceExposedAsBusiness() throws Exception {
        String url = "http://localhost:8084/petals/services/HelloServicePortService";
        INFO("Calling the business hello service exposed by the DSB");
        HelloService client = CXFHelper.getClientFromFinalURL(url, HelloService.class);
        String input = "Hello";
        String result = client.sayHello(input);
        
        INFO("Service returned '%s'", result);
        assertTrue(result.contains(input));
        OK("Valid response");
    }
}
