/**
 * 
 */
package org.petalslink.dsb.jbi.se.wsn.listeners;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.ow2.petals.component.framework.api.exception.PEtALSCDKException;
import org.ow2.petals.component.framework.api.message.Exchange;
import org.ow2.petals.component.framework.listener.AbstractJBIListener;
import org.ow2.petals.component.framework.util.SourceUtil;
import org.petalslink.dsb.jbi.se.wsn.AddressingHelper;
import org.petalslink.dsb.jbi.se.wsn.Component;
import org.petalslink.dsb.jbi.se.wsn.Constants;
import org.petalslink.dsb.jbi.se.wsn.NotificationEngine;
import org.petalslink.dsb.jbi.se.wsn.api.MonitoringService;
import org.petalslink.dsb.jbi.se.wsn.api.WSNException;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.NotificationHelper;
import org.w3c.dom.Document;

import com.ebmwebsourcing.wsaddressing10.api.element.Address;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.WsnbConstants;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.refinedabstraction.RefinedWsnbFactory;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.refinedabstraction.RefinedWsrfrpFactory;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.utils.WsrfrpException;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.ebmwebsourcing.wsstar.wsrfbf.services.faults.AbsWSStarFault;

/**
 * 
 * @author chamerling
 * 
 */
public class NotificationV2JBIListener extends AbstractJBIListener {

    // TODO : put in a common project
    static final String LOCATION_COMPONENT = "consumer.location.component";

    static final String LOCATION_CONTAINER = "consumer.location.container";

    static final String LOCATION_DOMAIN = "consumer.location.domain";

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.petals.component.framework.listener.AbstractJBIListener#
     * onNotificationMessage
     * (org.ow2.petals.component.framework.api.message.Exchange)
     */
    @Override
    public boolean onJBIMessage(Exchange exchange) {

        // bypass the old stuff and add new one...
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine(
                    String.format("We have a notification message with operation '%s'",
                            exchange.getOperation()));
        }

        NotificationEngine engine = getNotificationEngine();
        // can be null...
        MonitoringService monitoring = getWSNComponent().getMonitoringService();

        boolean response = true;
        NormalizedMessage normalizedMessage = null;
        Document document = null;
        Address address = null;
        String uuid = UUID.randomUUID().toString();

        try {
            if (this.getLogger().isLoggable(Level.FINE)) {
                for (final QName intf : exchange.getEndpoint().getInterfaces()) {
                    this.getLogger().fine("notification endpoint interface: " + intf);
                }
                this.getLogger()
                        .fine("notification endpoint service: "
                                + exchange.getEndpoint().getServiceName());
                this.getLogger().fine(
                        "notification endpoint name: " + exchange.getEndpoint().getEndpointName());
                this.getLogger()
                        .fine("notification operation name: " + exchange.getOperationName());
            }

            if (exchange.isActiveStatus()) {
                if (exchange.getFault() != null) {

                    if (this.getLogger().isLoggable(Level.WARNING)) {
                            this.getLogger().warning(
                                    "notification business fault message content: "
                                            + SourceUtil.createString(
                                                    exchange.getFault().getContent()));
                    }

                } else {
                    // We have map between the consumer reference which may be
                    // an external URL with an internal DSB endpoint which will
                    // be used to notify this subscriber.

                    // solution 1: change the subscriber address by adding the
                    // source of the current message. By doing this, we will
                    // need to use some WS-Addressing based stuff to send
                    // notifications.
                    normalizedMessage = exchange.getInMessage();
                    document = SourceUtil.createDocument(
                            normalizedMessage.getContent());

                    if (getLogger().isLoggable(Level.FINE)) {
                        try {
                            getLogger().fine(
                                    "Input message : " + com.ebmwebsourcing.easycommons.xml.XMLHelper
                                    .createStringFromDOMDocument(document));
                        } catch (TransformerException e) {
                            e.printStackTrace();
                        }
                    }

                    if (WsnbConstants.SUBSCRIBE_QNAME.getLocalPart().equals(exchange.getOperation().getLocalPart())) {
                        
                        document = SourceUtil.createDocument(
                                normalizedMessage.getContent());

                        com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe subscribe = RefinedWsnbFactory
                                .getInstance().getWsnbReader().readSubscribe(document);

                        // address =
                        // subscribe.getConsumerReference().getAddress();

                        // modify the address if needed. We add the source
                        // component if needed...
                        Address consumerAddress = subscribe.getConsumerReference().getAddress();
                        URI value = consumerAddress.getValue();
                        getLogger()
                                .log(Level.FINE, String.format("Subscribe with Initial address is '%s'", value));

                        // get the source component. It can be used if needed...
                        String component = exchange.getProperty(LOCATION_COMPONENT) != null ? exchange
                                .getProperty(LOCATION_COMPONENT).toString() : null;
                        String container = exchange.getProperty(LOCATION_CONTAINER) != null ? exchange
                                .getProperty(LOCATION_CONTAINER).toString() : null;
                        String domain = exchange.getProperty(LOCATION_DOMAIN) != null ? exchange
                                .getProperty(LOCATION_DOMAIN).toString() : null;

                        if (getLogger().isLoggable(Level.FINE)) {
                            getLogger()
                                    .log(Level.FINE,
                                            String.format(
                                                    "Source location is component='%s' container='%s' domain='%s'",
                                                    component, container, domain));
                        }

                        /*
                         * addLocation(consumerAddress, component, container,
                         * domain); System.out.println("New location : " +
                         * consumerAddress.getValue());
                         * 
                         * final EndpointReferenceType newReference =
                         * SOAUtil.getInstance()
                         * .getXmlObjectFactory().create(EndpointReferenceType
                         * .class); Address newAddress =
                         * SOAUtil.getInstance().getXmlObjectFactory()
                         * .create(Address.class);
                         * newAddress.setValue(consumerAddress.getValue());
                         * newReference.setAddress(newAddress);
                         * 
                         * final ReferenceParameters ref =
                         * SOAUtil.getInstance().getXmlObjectFactory()
                         * .create(ReferenceParameters.class);
                         * newReference.setReferenceParameters(ref);
                         * 
                         * subscribe.setConsumerReference(newReference);
                         */

                        if (getLogger().isLoggable(Level.FINE)) {
                            Document dom = Wsnb4ServUtils.getWsnbWriter().writeSubscribeAsDOM(
                                    subscribe);
                            try {
                                getLogger().fine(
                                        com.ebmwebsourcing.easycommons.xml.XMLHelper
                                                .createStringFromDOMDocument(dom));
                            } catch (TransformerException e) {
                            }
                        }
                        
                        if (monitoring != null) {
                        	try {
								monitoring.newSubscribeRequest(uuid, consumerAddress.getValue().toString(), null);
							} catch (WSNException e) {
								getLogger().warning("Can not send subscribe monitoring " + e.getMessage());
							}
                        }

                        // call the producer
                        final com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse subscribeResponse = engine
                                .getNotificationManager().getNotificationProducerEngine()
                                .subscribe(subscribe);
                        // set the response
                        document = RefinedWsnbFactory.getInstance().getWsnbWriter()
                                .writeSubscribeResponseAsDOM(subscribeResponse);
                        
                        if (monitoring != null) {
                        	try {
                        		String responseId = "";
                        		try {
									responseId = NotificationHelper.getSubscriptionID(subscribeResponse);
								} catch (NotificationException e) {
									getLogger().warning(e.getMessage());
									responseId = "?";
								}
								monitoring.newSubscribeResponse(uuid, responseId);
							} catch (WSNException e) {
								getLogger().warning("Can not send subscribe monitoring " + e.getMessage());
							}
                        }
                        
                        normalizedMessage = exchange.getOutMessage();
                        normalizedMessage.setContent(SourceUtil
                                .createStreamSource(document));
                        exchange.setOutMessage(normalizedMessage);

                    } else if (WsnbConstants.NOTIFY_QNAME.getLocalPart().equals(exchange.getOperation()
                            .getLocalPart())) {
                        
                        getLogger().fine("Got a notify message");
                        
                        com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify notify = RefinedWsnbFactory
                                .getInstance().getWsnbReader().readNotify(document);
                        
                        final List<com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.NotificationMessageHolderType> notificationMessageList = notify
                                .getNotificationMessage();
                        
                        if (notificationMessageList == null || notificationMessageList.size() != 1) {
                            exchange.setError(new Exception(
                                    "The CDK need one and only one notification message"));
                        }
                        
                        engine.getNotificationConsumerEngine().notify(notify);
                        
                    } else if (WsnbConstants.UNSUBSCRIBE_NAME.equals(exchange.getOperation()
                            .getLocalPart())) {
                        
                        getLogger().log(Level.FINE, "Got an unsubscribe message");
                        
                        document = SourceUtil.createDocument(
                                normalizedMessage.getContent());

                        com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Unsubscribe unsubscribe = RefinedWsnbFactory
                                .getInstance().getWsnbReader().readUnsubscribe(document);

						if (monitoring != null) {
							try {
								monitoring.newUnsubscribeRequest(uuid, "TODO");
							} catch (WSNException e) {
								getLogger().warning(
										"Can not send unsubscribe request monitoring "
												+ e.getMessage());
							}
						}
                        
                        final com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.UnsubscribeResponse unsubscribeResponse = engine
                                .getNotificationManager().getSubscriptionManagerEngine()
                                .unsubscribe(unsubscribe);
                        
						if (monitoring != null) {
							try {
								monitoring.newUnsubscribeResponse(uuid);
							} catch (WSNException e) {
								getLogger().warning(
										"Can not send unsubscribe response monitoring "
												+ e.getMessage());
							}
						}
                        
                        document = RefinedWsnbFactory.getInstance().getWsnbWriter()
                                .writeUnsubscribeResponseAsDOM(unsubscribeResponse);
                        normalizedMessage = exchange.getOutMessage();
                        normalizedMessage.setContent(SourceUtil.createStreamSource(document));
                        exchange.setOutMessage(normalizedMessage);

                    } else if (WsnbConstants.GET_CURRENT_MESSAGE_QNAME.getLocalPart().equals(exchange
                            .getOperation().getLocalPart())) {
                        getLogger().log(Level.WARNING, "GET_CURRENT_MESSAGE_QNAME not implemented");

                    } else if (WsnbConstants.RENEW_QNAME.getLocalPart().equals(exchange.getOperation()
                            .getLocalPart())) {
                        getLogger().log(Level.WARNING, "RENEW_QNAME not implemented");
                        
                    } else if ("GetResourceProperty".equals(exchange.getOperation().getLocalPart())) {
                        getLogger().log(Level.INFO, "Got an GetResourceProperty message");

                        document = SourceUtil.createDocument(
                                normalizedMessage.getContent());

                        QName qname = RefinedWsrfrpFactory.getInstance().getWsrfrpReader()
                                .readGetResourceProperty(document);
                        com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse res = engine
                                .getNotificationManager().getNotificationProducerEngine()
                                .getResourceProperty(qname);
                        document = RefinedWsrfrpFactory.getInstance().getWsrfrpWriter()
                                .writeGetResourcePropertyResponseAsDOM(res);

                        normalizedMessage = exchange.getOutMessage();
                        normalizedMessage.setContent(SourceUtil
                                .createStreamSource(document));
                        exchange.setOutMessage(normalizedMessage);
                    } else {
                        exchange.setError(new Exception(
                                String.format(
                                        "unable to identify the operation %s of the WS-Notification specifications, or not implemented",
                                        exchange.getOperation())));
                    }
                }
            }
        } catch (final PEtALSCDKException e) {
            exchange.setError(new Exception(e));
            // } catch (final WSNotificationFault f) {
            // try {
            // exchange.setFault(f);
            // } catch (final MessagingException e) {
            // exchange.setError(new Exception(e));
            // }
        } catch (WsnbException e) {
            exchange.setError(new Exception(e));
        } catch (AbsWSStarFault e) {
            exchange.setError(new Exception(e));
        } catch (MessagingException e) {
            exchange.setError(new Exception(e));
        } catch (WsrfrpException e) {
            exchange.setError(new Exception(e));
        }
        return response;
    }

    /**
     * @param address
     * @param component
     * @param container
     * @param domain
     */
    private void addLocation(Address address, String component, String container, String domain) {
        if (address == null) {
            return;
        }

        URI uri = address.getValue();
        if (AddressingHelper.isInternalService(uri)) {
            address.setValue(AddressingHelper.addLocation(uri, component, container, domain));
        } else if (AddressingHelper.isExternalService(uri)) {
            String tmp = uri.toString();
            if (!tmp.startsWith(Constants.DSB_EXTERNAL_SERVICE_NS)) {
                tmp = Constants.DSB_EXTERNAL_SERVICE_NS + "::" + uri.toString();
            }
            address.setValue(AddressingHelper.addLocation(URI.create(tmp), component, container,
                    domain));
        } else {
            // NOP
        }

    }

    NotificationEngine getNotificationEngine() {
        return ((Component) getComponent()).getNotificationEngine();
    }
    
    Component getWSNComponent() {
        return ((Component) getComponent());
    }
}
