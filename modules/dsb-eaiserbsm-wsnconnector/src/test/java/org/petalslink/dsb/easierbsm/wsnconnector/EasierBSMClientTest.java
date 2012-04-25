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

    /**
     * Check that the message the adapter sends are the right ones: well
     * translated from DSB model to monitoring layer one...
     * 
     * @throws Exception
     */
    public void testNotifyWithReports() throws Exception {

        final AtomicInteger calls = new AtomicInteger(0);
        final AtomicInteger fails = new AtomicInteger(0);
        final StringBuffer sb = new StringBuffer();

        EasierBSMClient client = new EasierBSMClient(EasierBSMConstants.RAWREPORT_TOPIC);
        // bypass the notification center by setting the monitoring sender here,
        // just for test purposes
        client.setMonitoringNotificationSender(new NotificationSender() {

            public void notify(Notify notify) throws NotificationException {
                System.out.println("Notified!");
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

        // create DSB report, will be translated into the right format in the
        // client implementation
        ReportListBean reportList = new ReportListBean();
        ReportBean report = new ReportBean();
        report.setExchangeId("d2ae05ee-cde1-426d-9c60-9813285f6129");
        report.setType("t1");
        report.setDate(System.currentTimeMillis());
        report.setConsumer("esb://http://www.webserviceX.NET/@StockQuoteSoapClientProxyEndpoint");
        report.setServiceName("Service");
        report.setOperation("operation");
        report.setItf("interface");
        report.setProvider("providerep");
        report.setEndpoint("endpoint");
        report.setContentLength(1234);
        report.setException(false);
        reportList.getReports().add(report);
        client.send(reportList);

        assertEquals(1, calls.get());
        assertEquals(0, fails.get());
    }
}
