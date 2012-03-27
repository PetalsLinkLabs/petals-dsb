/**
 * 
 */
package org.petalslink.dsb.integration.wsapi;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.integration.wsapi.mocks.ws.ResourceCreationTestService;
import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.ServiceEndpoint;
import org.w3c.dom.Document;

import static org.petalslink.dsb.integration.wsapi.Logs.INFO;
import static org.petalslink.dsb.integration.wsapi.Logs.KO;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * @author chamerling
 *
 */
public class KernelResourceCreationPubSubTest extends TestCase {
    
    static String dsbURL = "http://localhost:7600/petals/ws/";
    
    public static final QName topicUsed = new QName(
            "http://www.petalslink.org/resources/event/1.0", "CreationResourcesTopic", "tns");

    public static final String dialect = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";

    /**
     * @param name
     */
    public KernelResourceCreationPubSubTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testCreateEndpointAndReceiveNotification() throws Exception {
        String url = "http://localhost:8884/integration/testresource/ResourceCreationTestService";
        String localListener = "http://localhost:8885/integration/testresource/ListenerService";
        String dsbProducerService = "http://localhost:7600/petals/ws/NotificationProducer";
        
        final CountDownLatch latch = new CountDownLatch(1);
        INFO("Create local web service to be bound...");
        Service service = CXFHelper.getServiceFromFinalURL(url, ResourceCreationTestService.class,
                new ResourceCreationTestService() {

                    public void foo() {
                        // NOP
                    }
                });
        service.start();
        INFO("Local Service started on %s", url);
        
        INFO("Create a WSN service listener...");

        Service server = null;
        QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumer");
        QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumerService");
        QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumerPort");

        INotificationConsumer consumer = new INotificationConsumer() {
            public void notify(Notify notify) throws WsnbException {
                System.out
                        .println("Got a notify on HTTP service, this notification comes from the DSB itself...");
                
                latch.countDown();
 
                Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
                System.out.println("==============================");
                try {
                    XMLHelper.writeDocument(dom, System.out);
                } catch (TransformerException e) {
                }
                System.out.println("==============================");
            }
        };
        NotificationConsumerService consumerservice = new NotificationConsumerService(interfaceName,
                serviceName, endpointName, "NotificationConsumerService.wsdl", localListener, consumer);
 
        Exposer exposer = new CXFExposer();
        try {
            server = exposer.expose(consumerservice);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        INFO("WSN service listener created!");
        
        INFO("Subscribe to resource creation topic");
        HTTPProducerClient client = new HTTPProducerClient(dsbProducerService);
        String id = client.subscribe(topicUsed, localListener);
        INFO("Subscribed to notifications, ID is %s", id);
        
        INFO("Bind the service to the bus...");
        SOAPServiceBinder binder = CXFHelper.getClient(dsbURL, SOAPServiceBinder.class);
        List<ServiceEndpoint> endpoints = binder.bindWebService(url + "?wsdl");
        INFO("Service is bound, returned endpoints are :");
        for (ServiceEndpoint serviceEndpoint : endpoints) {
            INFO(" - Service : %s, Endpoint : %s, Interface %s", serviceEndpoint.getService(),
                    serviceEndpoint.getEndpoint(), serviceEndpoint.getItf());
        }

        
        INFO("We should receive a creation notification...");
        boolean called = latch.await(30, TimeUnit.SECONDS);
        if (!called) {
            KO("We did not receive any notification after creation");
            fail();
        }
    }
}
