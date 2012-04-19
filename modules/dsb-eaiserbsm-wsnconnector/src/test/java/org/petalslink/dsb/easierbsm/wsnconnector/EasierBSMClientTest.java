/**
 * 
 */
package org.petalslink.dsb.easierbsm.wsnconnector;

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.petalslink.dsb.easierbsm.wsnconnector.EasierBSMClient;
import org.petalslink.dsb.easierbsm.wsnconnector.EasierBSMConstants;
import org.petalslink.dsb.kernel.pubsub.service.NotificationCenter;
import org.petalslink.dsb.monitoring.api.ReportBean;
import org.petalslink.dsb.monitoring.api.ReportListBean;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.api.NotificationSender;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;

/**
 * @author chamerling
 * 
 */
public class EasierBSMClientTest extends TestCase {

    public void testNotifyWithReports() throws Exception {

        final AtomicInteger calls = new AtomicInteger(0);
        final AtomicInteger fails = new AtomicInteger(0);
        final StringBuffer sb = new StringBuffer();

        EasierBSMClient client = new EasierBSMClient(EasierBSMConstants.RAWREPORT_TOPIC);
        NotificationCenter.get().setNotifificationSender(new NotificationSender() {

            public void notify(Notify notify) throws NotificationException {
                System.out.println();
                calls.incrementAndGet();
            }

            public void notify(Document payload, QName topic, String dialect)
                    throws NotificationException {
                calls.incrementAndGet();
                System.out.printf("Got a notification on topic %s", topic.toString());
                System.out.println("Notification payload is ");
                // check the message format
                try {
                    String message = XMLHelper.createStringFromDOMDocument(payload);
                    System.out.println(message);
                    sb.append(message);
                } catch (TransformerException e) {
                    e.printStackTrace();
                    fails.incrementAndGet();
                }
            }
        });

        ReportListBean reportList = new ReportListBean();
        ReportBean report = new ReportBean();
        report.setConsumer("consumer");
        reportList.getReports().add(report);
        client.send(reportList);

        assertEquals(1, calls.get());
        assertEquals(0, fails.get());
    }
}
