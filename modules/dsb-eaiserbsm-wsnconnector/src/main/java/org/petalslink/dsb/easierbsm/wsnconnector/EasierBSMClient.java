/**
 * 
 */
package org.petalslink.dsb.easierbsm.wsnconnector;

import java.sql.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.petalslink.dsb.api.DSBException;
import org.petalslink.dsb.jaxbutils.SOAException;
import org.petalslink.dsb.jaxbutils.SOAJAXBContext;
import org.petalslink.dsb.kernel.pubsub.service.NotificationCenter;
import org.petalslink.dsb.monitoring.api.MonitoringClient;
import org.petalslink.dsb.monitoring.api.ReportBean;
import org.petalslink.dsb.monitoring.api.ReportListBean;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.api.NotificationSender;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;

import easybox.petalslink.com.esrawreport._1.EJaxbReportListType;
import easybox.petalslink.com.esrawreport._1.EJaxbReportTimeStampType;
import easybox.petalslink.com.esrawreport._1.EJaxbReportType;
import easybox.petalslink.com.esrawreport._1.ObjectFactory;

/**
 * This client is in charge of sending report list to easierBSM by using the
 * notification channel ie posting report to the right topic on the DSB and
 * which BSM sbscribed.
 * 
 * @author chamerling
 * 
 */
public class EasierBSMClient implements MonitoringClient {

    private static Logger logger = Logger.getLogger(EasierBSMClient.class.getName());

    private QName topic;

    private SOAJAXBContext context;

    private ObjectFactory factory = new ObjectFactory();
    
    private MonitoringNotificationSender sender;

    /**
     * 
     */
    public EasierBSMClient(QName topic) {
        this.topic = topic;

        try {
            context = SOAJAXBContext.getInstance();
            context.addOtherObjectFactory(ObjectFactory.class);
        } catch (SOAException e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petalslink.dsb.monitoring.api.MonitoringClient#send(org.petalslink
     * .dsb.monitoring.api.ReportListBean)
     */
    public void send(ReportListBean reportList) throws DSBException {
        if (reportList == null || reportList.getReports() == null) {
            final String message = "Can not send null reports...";
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(message);
            }
            throw new DSBException(message);
        }

        EJaxbReportListType report = new EJaxbReportListType();
        for (ReportBean reportBean : reportList.getReports()) {
            report.getReport().add(transform(reportBean));
        }

        // post message to report topic
        Document payload = null;
        try {
            // FIXME : why do we need this factory call? Without it we can not
            // unmarshall due to jaxb missing @XMLRootElement annotation error
            JAXBElement<EJaxbReportListType> jaxb = factory.createReportList(report);
            payload = SOAJAXBContext.getInstance().unmarshallAnyElement(jaxb);
        } catch (SOAException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Can not create the notification body in BSM client");
            }
            throw new DSBException(e);
        }

        if (logger.isLoggable(Level.FINE)) {
            try {
                logger.fine("Message to send to the notification engine from BSM client");
                logger.fine(XMLHelper.createStringFromDOMDocument(payload));
            } catch (TransformerException e) {
            }
        }
        
        NotificationCenter notificationCenter = NotificationCenter.get();
        if (notificationCenter == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Not able to find the notification center to send the notification");
            }
            throw new DSBException(
                    "Can not get the notification center to send new resource notification...");
        }

        NotificationSender sender = getMonitoringNotificationSender();
        if (sender == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Not able to find the notification sender to send the notification");
            }
            throw new DSBException(
                    "Can not get the notification sender from the notification center...");
        }

        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("Sending the notification to topic %s", topic));
            }
            sender.notify(payload, topic,
                    "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete");
        } catch (NotificationException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Can not send the notification", e);
            }
            throw new DSBException(e);
        }
    }

    /**
     * @param reportBean
     * @return
     */
    private EJaxbReportType transform(ReportBean reportBean) {
        EJaxbReportType result = new EJaxbReportType();
        result.setConsumerEndpointAddress(reportBean.getConsumer());
        result.setContentLength(reportBean.getContentLength());
        if (reportBean.getDate() != 0L) {
            final GregorianCalendar gCalendar = new GregorianCalendar();
            gCalendar.setTime(new Date(reportBean.getDate()));
            try {
                XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(gCalendar);
                result.setDateInGMT(xmlCalendar);
            } catch (DatatypeConfigurationException e) {
                e.printStackTrace();
            }
        }
        result.setDoesThisResponseIsAnException(reportBean.isException());
        result.setEndpointName(reportBean.getEndpoint());
        result.setExchangeId(reportBean.getExchangeId());
        if (reportBean.getItf() != null)
            result.setInterfaceQName(QName.valueOf(reportBean.getItf()));
        result.setOperationName(reportBean.getOperation());
        result.setProviderEndpointAddress(reportBean.getProvider());
        if (reportBean.getServiceName() != null)
            result.setServiceQName(QName.valueOf(reportBean.getServiceName()));
        if (reportBean.getType() != null) {
            try {
                result.setTimeStamp(EJaxbReportTimeStampType.fromValue(reportBean.getType()
                        .toLowerCase()));
            } catch (Exception e) {
            }
        }
        return result;
    }
    
    private synchronized MonitoringNotificationSender getMonitoringNotificationSender() {
        if (sender == null) {
            sender = new MonitoringNotificationSender(NotificationCenter.get().getManager()
                    .getNotificationProducerEngine());
        }
        return sender;
    }

}
