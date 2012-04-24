/**
 * 
 */
package org.petalsling.dsb.integration.esmanagement;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.petalsling.dsb.integration.esmanagement.services.ESManagementBindService;
import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.research.util.SOAException;
import com.ebmwebsourcing.easycommons.research.util.jaxb.SOAJAXBContext;
import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.WstopConstants;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;

import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbWsdl;
import esstar.petalslink.com.data.management.user._1.Bind;
import esstar.petalslink.com.data.management.user._1.BindResponse;
import esstar.petalslink.com.service.management.admin._1_0.AdminManagement;
import esstar.petalslink.com.service.management.user._1_0.UserManagement;

/**
 * Test all the notification part
 * 
 * @author chamerling
 * 
 */
public class NotificationTest extends TestCase {

    public static final String baseURL = "http://localhost:7600/petals/ws";

    static final QName CREATION_TOPIC = new QName("http://www.petalslink.org/resources/event/1.0",
            "CreationResourcesTopic", "bsm");

    static {
        try {
            SOAJAXBContext.getInstance().addOtherObjectFactory(
                    easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory.class,
                    com.ebmwebsourcing.wsstar.jaxb.notification.base.ObjectFactory.class,
                    com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.ObjectFactory.class,
                    com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.ObjectFactory.class,
                    esstar.petalslink.com.data.management.admin._1.ObjectFactory.class);
        } catch (SOAException e) {
            // do nothing
            e.printStackTrace();
        }
    }

    /**
     * @param name
     */
    public NotificationTest(String name) {
        super(name);
    }

    /**
     * Get the resource properties for a topic set ie get the list of topics
     * 
     * @throws Exception
     */
    public void testGetResourceProperties() throws Exception {
        AdminManagement client = CXFHelper.getClient(baseURL, AdminManagement.class);

        com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.GetResourcePropertyResponse response = client
                .getResourceProperty(WstopConstants.TOPIC_SET_QNAME);

        assertNotNull(response);

        Document document = SOAJAXBContext.getInstance().unmarshallAnyElement(response);

        String result = XMLHelper.createStringFromDOMDocument(document);
        System.out.println(result);

        // just test that we have a TOpicSet element
        assertTrue(result.contains("TopicSet"));
    }

    /**
     * Test to subscribe and then to send a notification : We should receive it
     * back on a local listener;
     * 
     * @throws Exception
     */
    public void testSubscribeAndPublish() throws Exception {
        
        final AtomicInteger i = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(1);
        String address = "http://localhost:7663/dsb/integration/esmanagement/LocalListener";
        String serviceURL = "http://localhost:7663/dsb/integration/esmanagement/LocalService";
        String dsbSubscribeAddress = baseURL + "/AdminManagement";
        
        AdminManagement admin = CXFHelper.getClient(baseURL, AdminManagement.class);
        UserManagement user = CXFHelper.getClient(baseURL, UserManagement.class);

        // create the localservice to bind
        System.out.println(">>> Creating service to bind...");
        Service s = CXFHelper.getServiceFromFinalURL(serviceURL, ESManagementBindService.class,
                new ESManagementBindService() {
                    public String foo(String bar) {
                        System.out.println("Got a call...");
                        return bar.toUpperCase();
                    }
                });

        s.start();
        System.out.println("<<< Business service Created!");

        // create the local listener which will receive notifications
        System.out.print(">>>Creating the local listener...");
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
        URL wsdlURL = NotificationTest.class.getResource("/NotificationConsumerService.wsdl");

        if (wsdlURL == null) {
            fail();
        }

        NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                serviceName, endpointName, wsdlURL.getFile(), address, consumer);
        Exposer exposer = new CXFExposer();
        Service server = null;

        try {
            server = exposer.expose(service);
            server.start();
            
            System.out.println("<<< Listener created!");

            // subscribe
            System.out.println(">>> Subscribe to receive notifications on the resource creation Topic...");

            // subscribe using the DSB library, not the CXF client and directly on the ESManagement interface..
            HTTPProducerClient client = new HTTPProducerClient(dsbSubscribeAddress);
            String id = client.subscribe(CREATION_TOPIC, address);

            System.out.println("<<< Subscribed, got subscription ID = " + id);

            // let's bind a service to receive a notification...
            System.out.println(">>> Binding the business service using the ESManagement API");
            Bind bind = new Bind();
            easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory factory = new easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory();
            JAXBElement<String> url = factory.createEJaxbWsdlUrl(serviceURL + "?wsdl");
            EJaxbWsdl wsdl = new EJaxbWsdl();
            wsdl.setUrl(url);
            bind.setWsdl(wsdl);
            BindResponse response = user.bind(bind);
            System.out.println("<<< Bound!");

            System.out.println(">>>> We should receive the notification, let's wait....");

            boolean wait = latch.await(20, TimeUnit.SECONDS);

            assertTrue(i.get() > 0);
            
            // let's directly send a notification to see if the listener receives it...

        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

}
