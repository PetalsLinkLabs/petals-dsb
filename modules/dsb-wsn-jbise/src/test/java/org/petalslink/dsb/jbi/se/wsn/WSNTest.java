/**
 * 
 * Copyright (c) 2012, PetalsLink
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 */
package org.petalslink.dsb.jbi.se.wsn;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.SOAUtil;
import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ebmwebsourcing.easycommons.xml.SourceHelper;
import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * @author chamerling
 * 
 */
public class WSNTest extends TestCase {

    Logger logger = Logger.getLogger(WSNTest.class.getName());

    static String address = "http://localhost:8878/petals/services/NotificationConsumerPortService";

    final static AtomicLong calls = new AtomicLong(0);

    public void testAddTopicAtRuntime() throws Exception {

        final NotificationEngine engine = new NotificationEngine(logger,
                QName.valueOf("{http://petals.ow2.org}/Service"),
                QName.valueOf("{http://petals.ow2.org}/Interface"), "Endpoint",
                new org.petalslink.dsb.service.client.saaj.Client());

        URL topicSet = WSNTest.class.getResource("/topicset.xml");
        URL tns = WSNTest.class.getResource("/tns.xml");
        engine.init(topicSet, tns);

        System.out.println("starting");
        // start a service which will be in charge of receiving notifications
        startServer();

        System.out.println("started");

        // create a subscription
        final QName topic = new QName("http://www.petalslink.org/integration/test/1.0",
                "BusinessIntegrationTopic", "itg");
        final QName oldtopic = new QName("http://www.petalslink.org/integration/test/1.0",
                "RemoveMe", "itg");
        
        String subscriber = address;
        String id1 = engine.subscribe(topic, subscriber);
        System.out.println("BusinessIntegrationTopic subscription ID = " + id1);

        String id2 = engine.subscribe(oldtopic, subscriber);
        System.out.println("RemoveMe subscription ID = " + id2);

        // send notifications and check that we receive them
        engine.notify(topic, XMLHelper.createDocumentFromString("<foo>bar</foo>"));
        
        Thread.sleep(2000L);
        
        assertEquals(1, calls.get());

        
        ScheduledExecutorService executors = Executors.newScheduledThreadPool(1);
        executors.scheduleAtFixedRate(new Runnable() {
            public void run() {
                System.out.println("Sending notification to a good topic");
                try {
                    engine.notify(topic, XMLHelper.createDocumentFromString("<foo>bar</foo>"));
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
        
        
      
        ScheduledExecutorService executorsOld = Executors.newScheduledThreadPool(1);
        executorsOld.scheduleAtFixedRate(new Runnable() {
            public void run() {
                System.out.println("### Sending notification to a topic which will be deleted");
                try {
                    engine.notify(oldtopic, XMLHelper.createDocumentFromString("<foo>bar</foo>"));
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0L, 50L, TimeUnit.MILLISECONDS);
        
        Thread.sleep(1000L);

        System.out.println("########################### Update Topics ###########################");
        // adding topics
        engine.updateTopicSet(SOAUtil.getInstance().getDocumentBuilderFactory()
                .newDocumentBuilder().parse(WSNTest.class.getResourceAsStream("/topicset1.xml")));
        System.out.println("########################### Topics Updated ###########################");


        Thread.sleep(10000L);
        //assertEquals(2, calls.get());
        
        System.out.println(calls);
        
        assertNotNull(id1);
        assertNotNull(id2);

        // send notifications

        // same in multihreaded mode...

    }

    private static void startServer() {
        System.out.println("****** CREATING LOCAL SERVER ******");

        // local address which will receive notifications
        System.out
                .println("Creating service which will receive notification messages from the DSB...");

        Service server = null;
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
                        .println(String
                                .format("Got a notify on HTTP service #%s, this notification comes from the DSB itself...",
                                        calls.incrementAndGet()));

                Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
                System.out.println("==============================");
                try {
                    XMLHelper.writeDocument(dom, System.out);
                } catch (TransformerException e) {
                }
                System.out.println("==============================");
            }
        };
        NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                serviceName, endpointName, "NotificationConsumerService.wsdl", address, consumer);

        Exposer exposer = new CXFExposer();
        try {
            server = exposer.expose(service);
            server.start();
            System.out.println("Local server is started and is ready to receive notifications");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

}
