/**
 * 
 */
package org.petalslink.dsb.servicepoller.api;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;

/**
 * @author chamerling
 * 
 */
public class ServicePollerServiceImplTest extends TestCase {

    public void testNullDocument() {
        final AtomicInteger i = new AtomicInteger(0);
        ServicePollerServiceAdapter service = new ServicePollerServiceAdapter(new ServicePoller() {

            public void stop(ServicePollerInformation toPoll, ServicePollerInformation replyTo)
                    throws ServicePollerException {

            }

            public String start(ServicePollerInformation toPoll, Document inputMessage,
                    String cronExpression, ServicePollerInformation reployTo)
                    throws ServicePollerException {
                // null document should not fire any problem from the upper
                // layer so I am here...
                i.incrementAndGet();
                
                return UUID.randomUUID().toString();
            }

            public boolean stop(String id) throws ServicePollerException {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean pause(String id) throws ServicePollerException {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean resume(String id) throws ServicePollerException {
                // TODO Auto-generated method stub
                return false;
            }
        });
        try {
            service.start(null, null, null, null);
        } catch (ServicePollerException e) {
            fail("No exception expected on null document input");
        }
        assertEquals(1, i.get());

    }

    public void testNotNullDocument() {
        final AtomicInteger i = new AtomicInteger(0);
        final AtomicInteger notNull = new AtomicInteger(0);
        ServicePollerServiceAdapter service = new ServicePollerServiceAdapter(new ServicePoller() {

            public void stop(ServicePollerInformation toPoll, ServicePollerInformation replyTo)
                    throws ServicePollerException {

            }

            public String start(ServicePollerInformation toPoll, Document inputMessage,
                    String cronExpression, ServicePollerInformation replyTo)
                    throws ServicePollerException {
                // null document should not fire any problem from the upper
                // layer so I am here...
                i.incrementAndGet();
                if (inputMessage != null) {
                    notNull.incrementAndGet();
                }
                // TODO : compare content
                return UUID.randomUUID().toString();
            }

            public boolean stop(String id) throws ServicePollerException {
                return false;
            }

            public boolean pause(String id) throws ServicePollerException {
                return false;
            }

            public boolean resume(String id) throws ServicePollerException {
                return false;
            }
        });

        Document document = null;
        try {
            File xml = new File(ServicePollerServiceImplTest.class.getResource("/input.xml")
                    .toURI());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(xml);
            document.getDocumentElement().normalize();
        } catch (Exception e1) {
            fail(e1.getMessage());
        }

        try {
            DocumentHandler dh = Utils.toDataHandler(document);
            service.start(null, dh, null, null);
        } catch (ServicePollerException e) {
            fail("No exception expected on null document input");
        }
        assertEquals(1, i.get());
        assertEquals(1, notNull.get());
    }

    public void testBadDocument() throws Exception {

    }

}
