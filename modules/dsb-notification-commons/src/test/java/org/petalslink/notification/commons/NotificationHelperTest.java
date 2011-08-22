/**
 * 
 */
package org.petalslink.notification.commons;

import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.NotificationHelper;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.refinedabstraction.RefinedWsnbFactory;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * @author chamerling
 * 
 */
public class NotificationHelperTest extends TestCase {

    static {
        Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
                new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
                new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
                new WsnbModelFactoryImpl());
    }

    /**
     * Just test that we can create a Notify message...
     */
    public void testAddPayload() {
        String producerAddress = "http://localhost:9998/foo/Producer";
        String endpointAddress = "http://localhost:9998/foo/Endpoint";
        String uuid = UUID.randomUUID().toString();
        // All the fields are required for the topic! If not, there will be an
        // exception in
        // com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbWriterImpl.writeNotifyAsDOM(Notify)
        QName topicUsed = new QName("http://dsb.petalslink.org/notification", "Sample", "dsbn");
        String dialect = "dialect";
        Document notifPayload = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            notifPayload = dbf.newDocumentBuilder()
                    .parse(NotificationHelperTest.class.getResourceAsStream("/notify.xml"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        try {
            Notify n = NotificationHelper.createNotification(producerAddress, endpointAddress,
                    uuid, topicUsed, dialect, notifPayload);

            try {
                final Document request = RefinedWsnbFactory.getInstance().getWsnbWriter()
                        .writeNotifyAsDOM(n);
                System.out.println(XMLHelper.createStringFromDOMDocument(request));
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }

        } catch (NotificationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
