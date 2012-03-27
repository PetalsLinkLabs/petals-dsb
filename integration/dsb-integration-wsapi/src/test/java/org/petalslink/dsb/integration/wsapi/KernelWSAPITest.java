/**
 * 
 */
package org.petalslink.dsb.integration.wsapi;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.integration.wsapi.mocks.ws.PingMockService;
import org.petalslink.dsb.ws.api.DSBInformationService;
import org.petalslink.dsb.ws.api.DSBWebServiceException;
import org.petalslink.dsb.ws.api.ExposerService;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.ServiceEndpoint;
import org.petalslink.dsb.ws.api.ServiceInformation;

import static org.petalslink.dsb.integration.wsapi.Logs.INFO;
import static org.petalslink.dsb.integration.wsapi.Logs.KO;
import static org.petalslink.dsb.integration.wsapi.Logs.OK;

import com.google.common.collect.Sets;

/**
 * Test the DSB kernel API
 * 
 * @author chamerling
 * 
 */
public class KernelWSAPITest extends TestCase {
    
    static String dsbURL = "http://localhost:7600/petals/ws/";


    /**
     * @param name
     */
    public KernelWSAPITest(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        INFO("Starting the DSB...");
        //startDSB();
        // wait that the DSB is started (can be long)...
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        INFO("Stopping the DSB...");
        // poll for stop...
        //stopDSB();
    }
    
    public void testGetAllServices() throws Exception {
       DSBInformationService info = CXFHelper.getClient(dsbURL, DSBInformationService.class);
       System.out.println(info.getWebServices());
    }

    public void testBindAndCallService() {


        String url = "http://localhost:8884/integration/testbing/PingMockService";
        final AtomicInteger calls = new AtomicInteger(0);
        INFO("Create local web service to be bound...");
        Service service = CXFHelper.getServiceFromFinalURL(url, PingMockService.class,
                new PingMockService() {

                    public String ping(String input) {
                        INFO("Ping service is called by the DSB with input %s", input);
                        calls.incrementAndGet();
                        return input;
                    }
                });

        service.start();

        INFO("Service started on %s", url);

        // get the number of bound services
        SOAPServiceBinder binder = CXFHelper.getClient(dsbURL, SOAPServiceBinder.class);
        try {
            Set<String> services = binder.getWebServices();
            INFO("Available bound services %d", services.size());

            if (services.contains(url + "?wsdl")) {
                System.out.println("Service is already bound...");
            }

            INFO("Binding web service...");
            List<ServiceEndpoint> endpoints = binder.bindWebService(url + "?wsdl");
            INFO("Service is bound, returned endpoints are :");
            for (ServiceEndpoint serviceEndpoint : endpoints) {
                INFO(" - Service : %s, Endpoint : %s, Interface %s", serviceEndpoint.getService(),
                        serviceEndpoint.getEndpoint(), serviceEndpoint.getItf());
            }
            if (endpoints == null || endpoints.size() == 0) {
                KO("No endpoints returned...");
            }

            Set<String> after = binder.getWebServices();
            boolean bound = after.contains(url + "?wsdl");
            INFO("Check that the service is really bound on the API: " + bound);

            // expose the service and call it
            INFO("Get the currently exposed services...");
            ServiceInformation info = CXFHelper.getClient(dsbURL, ServiceInformation.class);
            Set<String> webservices = info.getExposedWebServices();
            for (String string : webservices) {
                INFO("Exposed service %s", string);
            }

            INFO("Expose the bound service as Web service");
            for (ServiceEndpoint endpoint : endpoints) {
                INFO("Expose endpoint %s", endpoint);
                ExposerService exposer = CXFHelper.getClient(dsbURL, ExposerService.class);
                exposer.expose();
            }

            INFO("Wait that the endpoint has been exposed...");
            // TODO : polling will be better...
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }

            // make the diff to get the new URL
            INFO("Get the new service address");
            Set<String> newwebservices = info.getExposedWebServices();
            for (String string : newwebservices) {
                INFO("Exposed service %s", string);
            }

            Set<String> set = Sets.difference(newwebservices, webservices);
            INFO("New services");
            for (String string : set) {
                OK("New service %s", string);
            }
            
            // let's call the service
            INFO("Let's call the business service which is exposed by the DSB...");
            int i = 0;
            for (String string : set) {
                PingMockService client = CXFHelper.getClientFromFinalURL(string, PingMockService.class);
                String in = "Hello";
                String out = client.ping(in);
                INFO("Business Web service returned '%s'", out);
                if (in.equals(out)) {
                    OK("Good response!");
                }
                i++;
            }
            
            INFO("Web services called %d", i);

        } catch (DSBWebServiceException e) {
            KO("Ouch!");
            e.printStackTrace();
            fail("There is something wrong, check KO logs");
        } finally {
            INFO("Stopping local service");
            service.stop();
        }
    }
    
    
}
