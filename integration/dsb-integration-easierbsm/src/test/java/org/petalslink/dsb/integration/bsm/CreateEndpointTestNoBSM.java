/**
 * 
 */
package org.petalslink.dsb.integration.bsm;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.integration.bsm.mocks.BSMIntegrationCreateResourceService;
import org.petalslink.dsb.integration.bsm.mocks.BSMIntegrationService;
import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.petalslink.dsb.ws.api.ExposerService;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.ServiceEndpoint;
import org.petalslink.dsb.ws.api.ServiceInformation;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.google.common.collect.Sets;

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

    static final QName REPORTS_TOPIC = new QName("http://www.petalslink.org/rawreport/1.0",
            "RawReportTopic", "bsm");

    static {
        Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
                new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
                new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
                new WsnbModelFactoryImpl());
    }

    public void testCreateEndpointAndInvoke() throws Exception {

        // create a remote notification listener which will receive WSN
        // notification

        // subscribe to the kernel notifications

        // bind a service to the DSB

        final AtomicInteger i = new AtomicInteger(0);
        final AtomicInteger businessCalled = new AtomicInteger(0);

        final CountDownLatch latch = new CountDownLatch(2);

        // create the local listener
        String address = "http://localhost:7685/dsb/integration/bsm/LocalListener";
        String businessURL = "http://localhost:7686/integration/bsm/business/BSMIntegrationService";
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
                System.out
                        .println(">>>> Got a notify on local service coming from the DSB for rawreports...");

                Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
                try {
                    System.out.println(XMLHelper.createStringFromDOMDocument(dom));
                } catch (TransformerException e) {
                }

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
                    .println("Create the local service which will receive the notification we will invoke business service...");
            server = exposer.expose(service);
            server.start();

            Service business = CXFHelper.getServiceFromFinalURL(businessURL,
                    BSMIntegrationService.class, new BSMIntegrationService() {
                        public String sayHello() {
                            System.out.println("Business service Called!");
                            businessCalled.incrementAndGet();
                            return "Hello";
                        }
                    });

            business.start();

            System.out.println("Get the currently exposed services (before bind)...");
            ServiceInformation info = CXFHelper.getClient(dsbURL, ServiceInformation.class);
            Set<String> webservices = info.getExposedWebServices();
            for (String string : webservices) {
                System.out.printf("Exposed service %s", string);
                System.out.println();
            }

            // let's bind the service
            SOAPServiceBinder binder = CXFHelper.getClient(dsbURL, SOAPServiceBinder.class);
            Set<String> services = binder.getWebServices();
            List<ServiceEndpoint> endpoints = binder.bindWebService(businessURL + "?wsdl");
            Set<String> after = binder.getWebServices();

            for (ServiceEndpoint endpoint : endpoints) {
                System.out.printf("Expose endpoint %s as Web service", endpoint);
                ExposerService exposerService = CXFHelper.getClient(dsbURL, ExposerService.class);
                exposerService.expose();
            }

            System.out.println("Wait that the endpoints have been exposed...");
            // TODO : polling will be better...
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }

            // subscribe to rawreports
            System.out.println("Subscribe to receive raw reports notifications");
            HTTPProducerClient client = new HTTPProducerClient(dsbSubscribeAddress);
            String rawid = client.subscribe(REPORTS_TOPIC, address);

            System.out.println("Subscribed to rawreports, got ID = " + rawid);

            // let's invoke the service and we should receive notifications...
            System.out.println("Get the new service address");
            info = CXFHelper.getClient(dsbURL, ServiceInformation.class);

            Set<String> newwebservices = info.getExposedWebServices();
            for (String string : newwebservices) {
                System.out.printf(">> Exposed service %s", string);
                System.out.println();
            }

            Set<String> set = Sets.difference(newwebservices, webservices);
            System.out.println("New services");
            for (String string : set) {
                System.out.printf("New service %s", string);
                System.out.println();
            }

            // let's say that the service to call is the first one...
            if (set.size() == 0) {
                fail("Can not retrieve the service to call from the DSB API...");
            }

            String fullURL = new ArrayList<String>(set).get(0);
            BSMIntegrationService bsmIntegrationService = CXFHelper.getClientFromFinalURL(fullURL,
                    BSMIntegrationService.class);

            // let's call it, we should receive notifications...
            String out = bsmIntegrationService.sayHello();

            System.out.println("Received from business service '" + out + "'");

            System.out.println("We should receive the notifications, let's wait....");

            boolean wait = latch.await(20, TimeUnit.SECONDS);

            // should be more than one...
            assertTrue(i.get() > 0);

        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.stop();
            }
        }

    }

    /**
     * We should receive notification on message exchange...
     * 
     * @throws Exception
     */
    public void testCreateEndpoint() throws Exception {

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
