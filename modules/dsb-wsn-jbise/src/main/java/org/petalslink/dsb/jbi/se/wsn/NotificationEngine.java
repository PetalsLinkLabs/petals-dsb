/**
 * 
 */
package org.petalslink.dsb.jbi.se.wsn;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.petals.component.framework.AbstractComponent;
import org.ow2.petals.component.framework.ComponentWsdl;
import org.ow2.petals.component.framework.api.Wsdl;
import org.ow2.petals.component.framework.util.WSDLUtilImpl;
import org.petalslink.dsb.api.util.EndpointHelper;
import org.petalslink.dsb.jbi.se.wsn.api.MonitoringService;
import org.petalslink.dsb.jbi.se.wsn.api.Topic;
import org.petalslink.dsb.jbi.se.wsn.api.WSNException;
import org.petalslink.dsb.notification.commons.AbstractNotificationSender;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.NotificationHelper;
import org.petalslink.dsb.notification.commons.NotificationManagerImpl;
import org.petalslink.dsb.notification.commons.api.NotificationManager;
import org.petalslink.dsb.notification.service.NotificationProducerRPService;
import org.petalslink.dsb.service.client.Client;
import org.petalslink.dsb.service.client.ClientException;
import org.petalslink.dsb.service.client.Message;
import org.petalslink.dsb.service.client.MessageImpl;
import org.petalslink.dsb.service.client.WSAMessageImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsaddressing10.api.type.EndpointReferenceType;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.WsnbConstants;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.resource.datatypes.api.WsrfrConstants;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.UpdateResourceProperties;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.UpdateResourcePropertiesResponse;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.UpdateType;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.refinedabstraction.RefinedWsrfrpFactory;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.abstraction.TopicSetType;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.refinedabstraction.RefinedWstopFactory;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.engines.AbsNotificationConsumerEngine;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * @author chamerling
 * 
 */
public class NotificationEngine {

    private Logger logger;

    NotificationManager notificationManager;

    private QName serviceName;

    private QName interfaceName;

    private String endpointName;

    private AbsNotificationConsumerEngine notificationConsumerEngine;

    /**
     * This one is used to send notifications to service bus endpoints
     */
    private AbstractNotificationSender internalNotificationSender;

    Wsdl consumerWSDL;

    Wsdl producerWSDL;

    /**
     * Client injected by the JBI component, generally a JBI-based client
     */
    private Client client;

    private ServiceEngine serviceEngine;

	private MonitoringService monitoring;

    public NotificationEngine(Logger logger, QName serviceName, QName interfaceName,
            String endpointName, Client client, MonitoringService monitoringService) {
        super();
        this.logger = logger;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
        this.endpointName = endpointName;
        this.client = client;
        this.monitoring = monitoringService;
    }

    /**
     * Init from resources
     * 
     * @param topicSet
     * @param topicNamespaces
     */
    public void init(URL topicSet, URL topicNamespaces) {
        this.notificationManager = new NotificationManagerImpl(topicSet, topicNamespaces,
                serviceName, interfaceName, endpointName);
        this.init();
    }

    /**
     * Init from DOMs
     * 
     * @param topicSet
     * @param topicNamespaces
     */
    public void init(Document topicSet, Document topicNamespaces) {
        this.notificationManager = new NotificationManagerImpl(topicSet, topicNamespaces,
                serviceName, interfaceName, endpointName);
        this.init();
    }

    /**
     * 
     */
    protected void init() {
        this.internalNotificationSender = getHTTPSender();

        // The one which receives notifications from consumers (external), and
        // forward them to the notification engine
        this.notificationConsumerEngine = new AbsNotificationConsumerEngine(logger) {

            @Override
            public void notify(Notify notify) {
                // the goal of the component is to forward the notifiy messages
                // to the notification engine so that it is up to the
                // notification engine to forward the notification to all the
                // interested parties.
                if (logger.isLoggable(Level.FINE)) {
                    StringBuffer trace = new StringBuffer(
                            "--- Got a notify, forward to internal engine ---\n");
                    try {
                        Document doc = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
                        trace.append(XMLHelper.createStringFromDOMDocument(doc));
                    } catch (Exception e) {
                        trace.append("Serialization problem : ");
                        trace.append(e.getMessage());
                    }
                    trace.append("\n-------------------------\n");
                    logger.fine(trace.toString());
                }

                try {
                    internalNotificationSender.notify(notify);
                } catch (NotificationException e) {
                    // toulouse we'we got a problem!
                    e.printStackTrace();
                }
            }
        };
        consumerWSDL = loadDocument("WS-NotificationConsumer.wsdl");
        producerWSDL = loadDocument("WS-NotificationProducer.wsdl");

        this.serviceEngine = new ServiceEngine();
        this.serviceEngine.addService(new NotificationProducerRPService(null, null, null, null,
                null, this.getNotificationManager().getNotificationProducerEngine()), new QName[] {
                new QName(WsrfrConstants.WS_RESOURCE_NAMESPACE_URI, "GetResourceProperty"),
                new QName(WsrfrConstants.WS_RESOURCE_NAMESPACE_URI, "UpdateResourceProperties") });
    }

    /**
     * @param string
     * @return
     */
    private Wsdl loadDocument(String string) {
        try {
            return new ComponentWsdl(WSDLUtilImpl.createWsdlDescription(AbstractComponent.class
                    .getResource("/" + string)));
        } catch (WSDLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return the notificationManager
     */
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    /**
     * @return the consumerWSDL
     */
    public Wsdl getConsumerWSDL() {
        return consumerWSDL;
    }

    /**
     * @return the producerWSDL
     */
    public Wsdl getProducerWSDL() {
        return producerWSDL;
    }

    /**
     * @return
     */
    public AbsNotificationConsumerEngine getNotificationConsumerEngine() {
        return this.notificationConsumerEngine;
    }

    public ServiceEngine getServiceEngine() {
        return this.serviceEngine;
    }

    protected AbstractNotificationSender getHTTPSender() {
        return new AbstractNotificationSender(this.getNotificationManager()
                .getNotificationProducerEngine()) {

            @Override
            protected String getProducerAddress() {
                return "petals://" + endpointName;
            }
            
			@Override
			protected void preNotify(Document payload, QName topic,
					String dialect, String uuid) {
				if (getMonitoringService() != null) {
					try {
						getMonitoringService().newInNotifyInput(uuid, payload, getTopic(topic),
								System.currentTimeMillis());
					} catch (WSNException e) {
					}
				}
			}

			@Override
			protected void postNotify(Document payload, QName topic,
					String dialect, String uuid) {
				if (getMonitoringService() != null) {
					try {
						getMonitoringService().newInNotifyOutput(uuid, payload, getTopic(topic),
								System.currentTimeMillis());
					} catch (WSNException e) {
					}
				}
			}

            @Override
            protected void doNotify(Notify notify, String producerAddress,
                    EndpointReferenceType currentConsumerEdp, String subscriptionId, QName topic,
                    String dialect, String uuid) throws NotificationException {

                if (currentConsumerEdp == null || currentConsumerEdp.getAddress() == null
                        || currentConsumerEdp.getAddress().getValue() == null) {
                    // no address found...
                    logger.fine("No address found, do not send notification");
                    return;
                }

                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Need to send the message to a subscriber which is : "
                            + currentConsumerEdp.getAddress().getValue() + " on topic " + topic);
                }
                
                Topic t = new Topic();
            	t.name = topic.getLocalPart();
            	t.ns = topic.getNamespaceURI();
            	t.prefix = topic.getPrefix();

                // we restrict sending messages directly to the HTTP endpoint

                // we use a WSA endpoint to send the notification...
                // extract data from address
                URI uri = currentConsumerEdp.getAddress().getValue();
                Message message = null;

                if (EndpointHelper.isDSBService(uri)) {
                    logger.warning("This client does not handle DSB services");
                    return;
                } else if (AddressingHelper.isExternalService(uri)) {
                    message = new MessageImpl();
                } else {
                    logger.warning("This client does not handle internal services");
                    return;
                }

                try {
                    final Document payload = Wsnb4ServUtils.getWsnbWriter()
                            .writeNotifyAsDOM(notify);
                    message.setEndpoint(currentConsumerEdp.getAddress().getValue().toString());
                    message.setPayload(payload);
                    message.setOperation(WsnbConstants.NOTIFY_QNAME);

                    // TODO : Fire and forget with thread executor
                    client.sendReceive(message);
                    
                    if (getMonitoringService() != null) {
						try {
							getMonitoringService().newOutNotify(
									uuid, payload,
									currentConsumerEdp.getAddress().getValue()
											.toString(), t,
									System.currentTimeMillis());
						} catch (WSNException e) {
						}
                    }

                    Stats.getInstance().newOutNotifyCall(topic);

                } catch (ClientException e) {
                	if (getMonitoringService() != null) {
						try {
							getMonitoringService().newOutNotifyError(
									uuid, null,
									currentConsumerEdp.getAddress().getValue()
											.toString(), t,
									System.currentTimeMillis(), e);
						} catch (WSNException e1) {
						}
                    }
                	
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE,
                                "Client got error while sending notification to "
                                        + currentConsumerEdp.getAddress().getValue() + " on topic "
                                        + topic, e);
                    } else {
                        logger.log(Level.INFO,
                                "Client got error while sending notification to endpoint "
                                        + currentConsumerEdp.getAddress().getValue() + " on topic "
                                        + topic);
                    }
                } catch (WsnbException e) {
                	if (getMonitoringService() != null) {
						try {
							getMonitoringService().newOutNotifyError(
									uuid, null,
									currentConsumerEdp.getAddress().getValue()
											.toString(), t,
									System.currentTimeMillis(), e);
						} catch (WSNException e1) {
						}
                    }
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE,
                                "WSN error while sending notification to "
                                        + currentConsumerEdp.getAddress().getValue() + " on topic "
                                        + topic, e);
                    } else {
                        logger.log(Level.INFO, "WSN error while sending notification to endpoint "
                                + currentConsumerEdp.getAddress().getValue() + " on topic " + topic);
                    }
                }

                // need to map between the address and the DSB endpoint to send
                // the message to... then we may use some WS-Addressing thing to
                // pass the initial address...
            }
        };
    }

    protected AbstractNotificationSender getDSBSender() {
        return new AbstractNotificationSender(this.getNotificationManager()
                .getNotificationProducerEngine()) {

            @Override
            protected String getProducerAddress() {
                return "jbi://" + endpointName;
            }

            @Override
            protected void doNotify(Notify notify, String producerAddress,
                    EndpointReferenceType currentConsumerEdp, String subscriptionId, QName topic,
                    String dialect, String uuid) throws NotificationException {

                if (currentConsumerEdp == null || currentConsumerEdp.getAddress() == null
                        || currentConsumerEdp.getAddress().getValue() == null) {
                    // no address found...
                    logger.fine("No address found, do not send notification");
                    return;
                }

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Need to send the message to a subscriber which is : "
                            + currentConsumerEdp.getAddress().getValue());
                }

                // we use a WSA endpoint to send the notification...
                // extract data from address
                URI uri = currentConsumerEdp.getAddress().getValue();
                Message message = null;

                if (EndpointHelper.isDSBService(uri)) {
                    message = new MessageImpl();
                    message.setEndpoint(EndpointHelper.getEndpoint(uri));
                    message.setService(EndpointHelper.getService(uri));

                } else if (AddressingHelper.isExternalService(uri)) {
                    message = new WSAMessageImpl(uri.toString());
                } else {
                    System.out.println("Internal service : TODO NotificationEngine class!");
                    return;
                }

                try {
                    final Document payload = Wsnb4ServUtils.getWsnbWriter()
                            .writeNotifyAsDOM(notify);

                    message.setPayload(payload);
                    message.setOperation(WsnbConstants.NOTIFY_QNAME);
                    client.fireAndForget(message);
                } catch (ClientException e) {
                    e.printStackTrace();
                } catch (WsnbException e) {
                    e.printStackTrace();
                }

                // need to map between the address and the DSB endpoint to send
                // the message to... then we may use some WS-Addressing thing to
                // pass the initial address...
            }
        };
    }

    /**
     * Subscribe to a topic.
     * 
     * @param topic
     *            the topic to subscribe to
     * @param subscriber
     *            the subscriber endpoint
     * @return the subscription UUID
     */
    public String subscribe(QName topic, String subscriber) {
        String result = null;
        try {
            Subscribe subscribe = NotificationHelper.createSubscribe(subscriber, topic);
            final com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse response = this
                    .getNotificationManager().getNotificationProducerEngine().subscribe(subscribe);
            if (response != null && response.getSubscriptionReference() != null
                    && response.getSubscriptionReference().getReferenceParameters() != null) {
                result = Wsnb4ServUtils.getSubscriptionIdFromReferenceParams(response
                        .getSubscriptionReference().getReferenceParameters());

            }
        } catch (Exception e) {
            e.getMessage();
            logger.warning(e.getMessage());
        }
        return result;
    }

    /**
     * Send a notify to the engine. Note : Should be async
     * 
     * @param topic
     *            the topic to send the notification to
     * @param payload
     *            the business message
     */
    public void notify(QName topic, Document payload) {
        try {
            Notify notify = NotificationHelper.createNotification(null, null, null, topic,
                    "http://www.w3.org/TR/1999/REC-xpath-19991116", payload);
            this.notificationConsumerEngine.notify(notify);
        } catch (NotificationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update the engine with the given topic list
     * 
     * @param topicSet
     */
    public void updateTopicSet(List<Topic> topicSet) {
    	if (topicSet == null) {
    		return;
    	}
    	this.updateTopicSet(TopicSetHelper.getWSNDocument(topicSet));
    }

    /**
     * Set a new topic set with the DOM content. Note that it will ne nice to
     * synchronize this of to add a flag somewhere to pause current
     * notifications beacuse nothing is synchronized...
     */
    public void updateTopicSet(Document topicSet) {
        if (topicSet == null) {
            logger.warning("Can not set a null topicset");
            return;
        }

        try {
            TopicSetType newTopicSetRP = Wsnb4ServUtils.getWstopReader().readTopicSetType(topicSet);
            Document topicSetAsDOM = RefinedWstopFactory.getInstance().getWstopWriter()
                    .writeTopicSetTypeAsDOM(newTopicSetRP);

            List<Element> properties = new ArrayList<Element>();
            properties.add(topicSetAsDOM.getDocumentElement());

            UpdateType content = RefinedWsrfrpFactory.getInstance().createUpdateType(properties);
            UpdateResourceProperties update = RefinedWsrfrpFactory.getInstance()
                    .createUpdateResourceProperties(content);
            UpdateResourcePropertiesResponse response = this.notificationManager
                    .getNotificationProducerEngine().updateResourceProperties(update);
            
			if (logger.isLoggable(Level.INFO)) {
				if (response == null) {
					logger.fine("No response returned from the engine in update topicset");
				} else {
					logger.info("Topicset update response "
							+ XMLHelper
									.createStringFromDOMDocument(RefinedWsrfrpFactory
											.getInstance()
											.getWsrfrpWriter()
											.writeUpdateResourcePropertiesResponseAsDOM(
													response)));
				}
			}
            
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.WARNING, "Can not update the resources", e);
            } else {
                logger.log(Level.WARNING, "Can not update the resources : " + e.getMessage());
            }
        }
        logger.info("Topics has been updated");
    }

	/**
	 * Get the current topics deployed in the component
	 * 
	 * @return
	 */
	public List<Topic> getTopics() {
		List<Topic> topics = new ArrayList<Topic>();

		// get them from the runtime...
		// TODO : We can cache them here instead of making a call...

		TopicSetType tst = this.notificationManager.getTopicSet();
		if (logger.isLoggable(Level.FINE)) {
			try {
				logger.fine("Current topicset "
						+ XMLHelper.createStringFromDOMDocument(Wsnb4ServUtils
								.getWstopWriter().writeTopicSetTypeAsDOM(tst)));
			} catch (Exception e1) {
				logger.warning(e1.getMessage());
			}
		}

		for (Element e : tst.getTopicsTrees()) {
			String nodeName = e.getNodeName();
			String prefix = null;
			if (nodeName.contains(":")) {
				prefix = nodeName.substring(0, nodeName.lastIndexOf(':'));
				nodeName = nodeName.substring(nodeName.lastIndexOf(':') + 1);
			}
			String ns = e.getAttribute("xmlns:" + prefix);
			Topic t = new Topic();
			t.name = nodeName;
			t.prefix = prefix;
			t.ns = ns;
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Topic " + t);
			}
			topics.add(t);
		}		

		return topics;
	}
	
	protected MonitoringService getMonitoringService() {
		return this.monitoring;
	}
	
	public Topic getTopic(QName topic) {
		if (topic == null) {
			return new Topic();			
		}
		Topic t = new Topic();
     	t.name = topic.getLocalPart();
     	t.ns = topic.getNamespaceURI();
     	t.prefix = topic.getPrefix();
     	return t;
	}
}
