/**
 * 
 */
package org.petalslink.dsb.integration.bsm;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.integration.bsm.mocks.BSMIntegrationCreateResourceService;
import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.ServiceEndpoint;

import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;

/**
 * Test that when we create an enpoint and that someone subscribed to the right
 * topic, we get notification...
 * 
 * @author chamerling
 * 
 */
public class CreateEndpointTestNoBSM extends TestCase {

    static String dsbURL = "http://localhost:7600/petals/ws";

    static final QName CREATION_TOPIC = new QName("http://www.petalslink.org/resources/event/1.0",
            "CreationResourcesTopic", "bsm");

    /**
     * We should receive a notification with the newly resource ID
     * 
     * @throws Exception
     */
    public void testCreateEndpointAndLocalListener() throws Exception {

        // create a remote notification listener which will receive WSN
        // notification

        // subscribe to the kernel notifications

        // bind a service to the DSB

    }

    /**
     * We should receive notification on message exchange...
     * 
     * @throws Exception
     */
    public void testCreateEndpointAndInvoke() throws Exception {

        final AtomicInteger i = new AtomicInteger(0);

        final CountDownLatch latch = new CountDownLatch(1);

        // create the local listener
        String address = "http://localhost:7683/dsb/integration/bsm/LocalListener";
        String businessURL = "http://localhost:7684/integration/bsm/business/BSMIntegrationCreateResourceService";
        String dsbSubscribeAddress = dsbURL + "/NotificationProducer";

        QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumer");
        QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumerService");
        QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumerPort");
        // expose the service
        INotificationConsumer consumer = new INotificationConsumer() {
            public void notify(Notify notify) throws WsnbException {
                System.out.println(">>>> Got a notify on local service coming from the DSB...");
                i.incrementAndGet();
                latch.countDown();
            }
        };

        URL wsdlURL = CreateEndpointTestNoBSM.class
                .getResource("/NotificationConsumerService.wsdl");

        if (wsdlURL == null) {
            fail();
        }

        NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                serviceName, endpointName, wsdlURL.getFile(), address, consumer);
        Exposer exposer = new CXFExposer();

        Service server = null;
        try {
            System.out
                    .println("Create the local service which will receive the notification when a new endpoint is created...");
            server = exposer.expose(service);
            server.start();

            // subscribe
            System.out.println("Subscribe to receive resource creation notification");
            HTTPProducerClient client = new HTTPProducerClient(dsbSubscribeAddress);
            String id = client.subscribe(CREATION_TOPIC, address);

            System.out.println("Subscribed, got ID = " + id);

            System.out
                    .println("Bind the service so that we receive a notification on resource creation");
            Service business = CXFHelper.getServiceFromFinalURL(businessURL,
                    BSMIntegrationCreateResourceService.class,
                    new BSMIntegrationCreateResourceService() {
                        public void foo() {
                            System.out.println("Called!");
                        }
                    });

            business.start();

            // let's bind the service
            SOAPServiceBinder binder = CXFHelper.getClient(dsbURL, SOAPServiceBinder.class);
            List<ServiceEndpoint> endpoints = binder.bindWebService(businessURL + "?wsdl");

            System.out.println("We should receive the notification, let's wait....");
            
            boolean wait = latch.await(20, TimeUnit.SECONDS);

            assertTrue(i.get() > 0);

        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.stop();
            }
        }

    }
}
