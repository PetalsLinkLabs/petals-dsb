/**
 * 
 */
package org.petalslink.dsb.integration.wsapi;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.notification.client.http.simple.HTTPConsumerClient;
import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.petalslink.dsb.integration.wsapi.Logs.INFO;
import static org.petalslink.dsb.integration.wsapi.Logs.KO;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * In this test, we assume that the WSN engine is installed in the DSB and that
 * we can subscribe and receive notifications from the DSB.
 * 
 * @author chamerling
 * 
 */
public class BusinessPubSubTest extends TestCase {

    static final String PUBSUB_ENDPOINT = "http://localhost:8084/petals/services/NotificationProducerPortService";

    static final String NOTIFY_ENDPOINT = "http://localhost:8084/petals/services/NotificationConsumerPortService";

    static final String LOCAL_SUBSCRIBER_ENDPOINT = "http://localhost:9998/test/integration/Listener";

    public static final QName BUSINESS_TOPIC = new QName(
            "http://www.petalslink.org/integration/test/1.0", "BusinessIntegrationTopic", "tns");

    public static final String DIALECT = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";

    public void testPubSub() throws Exception {
        INFO("Create a WSN service listener...");

        Service server = null;
        QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumer");
        QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumerService");
        QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                "NotificationConsumerPort");

        final CountDownLatch latch = new CountDownLatch(1);

        INotificationConsumer consumer = new INotificationConsumer() {
            public void notify(Notify notify) throws WsnbException {
                System.out
                        .println("Got a notify on HTTP service, this notification comes from the DSB business engine itself...");

                Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
                System.out.println("==============================");
                try {
                    XMLHelper.writeDocument(dom, System.out);
                } catch (TransformerException e) {
                }
                System.out.println("==============================");
                
                latch.countDown();
            }
        };
        NotificationConsumerService consumerservice = new NotificationConsumerService(
                interfaceName, serviceName, endpointName, "NotificationConsumerService.wsdl",
                LOCAL_SUBSCRIBER_ENDPOINT, consumer);

        Exposer exposer = new CXFExposer();
        try {
            server = exposer.expose(consumerservice);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        INFO("WSN service listener created!");

        INFO("Subscribe to some buniness topic...");
        HTTPProducerClient client = new HTTPProducerClient(PUBSUB_ENDPOINT);
        String id = null;
        try {
            id = client.subscribe(BUSINESS_TOPIC, LOCAL_SUBSCRIBER_ENDPOINT);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        INFO("Subscribed to notifications, ID is %s!", id);

        INFO("Send a notification to the business engine, we should receive it...");
        final HTTPConsumerClient consumerClient = new HTTPConsumerClient(NOTIFY_ENDPOINT);

        Runnable r = new Runnable() {
            public void run() {
                try {
                    INFO("Sending notification in another thread...");
                    consumerClient.notify(XMLHelper.createDocumentFromString("<foo>bar</foo>"),
                            BUSINESS_TOPIC);
                    INFO("Notification sent!");
                } catch (NotificationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread t = new Thread(r);
        t.start();

        INFO("Waiting for a notification...");
        boolean called = latch.await(30, TimeUnit.SECONDS);
        if (!called) {
            KO("We did not receive any notification after notify...");
            fail();
        }
    }
}
