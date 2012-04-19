/**
 * 
 */
package org.petalslink.dsb.easierbsm.wsnconnector;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.petalslink.dsb.api.DSBException;
import org.petalslink.dsb.api.ResourceConstants;
import org.petalslink.dsb.api.ServiceEndpoint;
import org.petalslink.dsb.api.util.ResourceIdBuilder;
import org.petalslink.dsb.jaxbutils.SOAException;
import org.petalslink.dsb.jaxbutils.SOAJAXBContext;
import org.petalslink.dsb.kernel.pubsub.service.NotificationCenter;
import org.petalslink.dsb.monitoring.api.MonitoringAdminClient;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.api.NotificationSender;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;

import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbResourceIdentifier;
import easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory;

/**
 * Note : This is a way to keep BSM and DSB lightly coupled...
 * 
 * @author chamerling
 * 
 */
public class EasierBSMAdminClient implements MonitoringAdminClient {

    private static Logger logger = Logger.getLogger(EasierBSMAdminClient.class.getName());

    private QName topic;

    private SOAJAXBContext context;

    /**
     * 
     */
    public EasierBSMAdminClient(QName topic) {
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
     * @see org.petalslink.dsb.monitoring.api.MonitoringAdminClient#
     * createMonitoringEndpoint(org.petalslink.dsb.api.ServiceEndpoint)
     */
    public void createMonitoringEndpoint(ServiceEndpoint serviceEndpoint) throws DSBException {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Creating BSM monitoring endpoint for service " + serviceEndpoint);
        }

        if (serviceEndpoint == null) {
            final String message = "Can not create monitoring endpoint from null endpoint...";
            throw new DSBException(message);
        }

        // we post a message to the resource creation topic, let's use a service
        // for that to not duplicate the code...
        EJaxbResourceIdentifier rid = new EJaxbResourceIdentifier();
        rid.setId(ResourceIdBuilder.getId(serviceEndpoint));
        rid.setResourceType(ResourceConstants.ENDPOINT);

        Document payload = null;
        try {
            payload = SOAJAXBContext.getInstance().unmarshallAnyElement(rid);
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

        NotificationSender sender = notificationCenter.getSender();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.monitoring.api.MonitoringAdminClient#
     * deleteMonitoringEndpoint(org.petalslink.dsb.api.ServiceEndpoint)
     */
    public void deleteMonitoringEndpoint(ServiceEndpoint serviceEndpoint) throws DSBException {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Deleting BSM monitoring endpoint for service " + serviceEndpoint);
        }

        if (serviceEndpoint == null) {
            final String message = "Can not delete monitoring endpoint from null endpoint...";
            throw new DSBException(message);
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Deleting monitoring endpoint for easierBSM is not implemented");
        }
    }
}
