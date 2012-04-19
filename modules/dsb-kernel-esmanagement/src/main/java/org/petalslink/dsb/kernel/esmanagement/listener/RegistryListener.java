/**
 * 
 */
package org.petalslink.dsb.kernel.esmanagement.listener;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.objectweb.fractal.fraclet.annotation.annotations.FractalComponent;
import org.objectweb.fractal.fraclet.annotation.annotations.Interface;
import org.objectweb.fractal.fraclet.annotation.annotations.LifeCycle;
import org.objectweb.fractal.fraclet.annotation.annotations.Monolog;
import org.objectweb.fractal.fraclet.annotation.annotations.Provides;
import org.objectweb.fractal.fraclet.annotation.annotations.type.LifeCycleType;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.petals.util.oldies.LoggingUtil;
import org.petalslink.dsb.api.DSBException;
import org.petalslink.dsb.api.ServiceEndpoint;
import org.petalslink.dsb.api.util.ResourceIdBuilder;
import org.petalslink.dsb.jaxbutils.SOAException;
import org.petalslink.dsb.jaxbutils.SOAJAXBContext;
import org.petalslink.dsb.kernel.pubsub.service.NotificationCenter;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.api.NotificationSender;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;

import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbResourceIdentifier;

/**
 * This listener sends notifications to the management tool when endpoints are
 * created or destroyed. Up to the management tool to process the information
 * and query the runtime...
 * 
 * @author chamerling
 * 
 */
@org.petalslink.dsb.annotations.registry.RegistryListener
@FractalComponent
@Provides(interfaces = { @Interface(name = "service", signature = org.petalslink.dsb.kernel.api.messaging.RegistryListener.class) })
public class RegistryListener implements org.petalslink.dsb.kernel.api.messaging.RegistryListener {

    @Monolog(name = "logger")
    private Logger logger;

    private LoggingUtil log;

    public static final QName topicUsed = new QName(
            "http://www.petalslink.org/resources/event/1.0", "CreationResourcesTopic", "tns");

    public static final String dialect = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";

    private SOAJAXBContext context;

    @LifeCycle(on = LifeCycleType.START)
    protected void start() {
        this.log = new LoggingUtil(this.logger);
        if (log.isDebugEnabled()) {
            log.debug("Starting the esmanagement registry listener");
        }
        
        try {
            context = SOAJAXBContext.getInstance();
            context.addOtherObjectFactory(easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory.class);
        } catch (SOAException e) {
        }
    }

    @LifeCycle(on = LifeCycleType.STOP)
    protected void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping the esmanagement registry listener");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petalslink.dsb.kernel.api.messaging.RegistryListener#onRegister(org
     * .petalslink.dsb.api.ServiceEndpoint)
     */
    public void onRegister(ServiceEndpoint endpoint) throws DSBException {
        if (log.isDebugEnabled()) {
            log.debug("esmanagement endpoint registry listener, send a notification to the engine...");
        }

        EJaxbResourceIdentifier rid = new EJaxbResourceIdentifier();
        rid.setId(ResourceIdBuilder.getId(endpoint));
        rid.setResourceType("endpoint");

        Document payload = null;
        try {
            payload = SOAJAXBContext.getInstance().unmarshallAnyElement(rid);
        } catch (SOAException e) {
            if (log.isWarnEnabled()) {
                log.warning("Can not create the notification body in esmanagement registry listener module");
            }
            throw new DSBException(e);
        }

        if (log.isDebugEnabled()) {
            try {
                log.debug("Message to send to the notification engine from esmanagement listener");
                log.debug(XMLHelper.createStringFromDOMDocument(payload));
            } catch (TransformerException e) {
            }
        }

        NotificationCenter notificationCenter = NotificationCenter.get();
        if (notificationCenter == null) {
            if (log.isWarnEnabled()) {
                log.warning("Not able to find the notification center to send the notification");
            }
            throw new DSBException(
                    "Can not get the notification center to send new resource notification...");
        }

        NotificationSender sender = notificationCenter.getSender();
        if (sender == null) {
            if (log.isWarnEnabled()) {
                log.warning("Not able to find the notification sender to send the notification");
            }
            throw new DSBException(
                    "Can not get the notification sender from the notification center...");
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Sending the notification to topic %s", topicUsed);
            }
            sender.notify(payload, topicUsed, dialect);
        } catch (NotificationException e) {
            if (log.isWarnEnabled()) {
                log.warning("Can not send the notification", e);
            }
            throw new DSBException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petalslink.dsb.kernel.api.messaging.RegistryListener#onUnregister
     * (org.petalslink.dsb.api.ServiceEndpoint)
     */
    public void onUnregister(ServiceEndpoint endpoint) throws DSBException {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering an endpoint in esmanagement listener");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.kernel.api.messaging.RegistryListener#getName()
     */
    public String getName() {
        return "ESManagementRegistryListener";
    }

}
