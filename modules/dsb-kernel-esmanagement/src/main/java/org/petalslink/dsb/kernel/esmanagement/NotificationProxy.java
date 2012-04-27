/**
 * 
 */
package org.petalslink.dsb.kernel.esmanagement;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rpw_2.InvalidResourcePropertyQNameFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnavailableFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.research.util.SOAException;
import com.ebmwebsourcing.easycommons.research.util.jaxb.SOAJAXBContext;
import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.NotificationMessageHolderType;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Notify;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Subscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.SubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.TopicExpressionType;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Unsubscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.UnsubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.GetResourcePropertyResponse;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * @author chamerling
 * 
 */
public class NotificationProxy {

    static final QName CREATION_TOPIC = new QName("http://www.petalslink.org/resources/event/1.0",
            "CreationResourcesTopic", "bsm");

    static final QName RAWREPORT_TOPIC = new QName("http://www.petalslink.org/rawreport/1.0",
            "RawReportTopic", "bsm");

    static Map<String, QName> topics;

    static {
        try {
            SOAJAXBContext.getInstance().addOtherObjectFactory(
                    easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory.class,
                    com.ebmwebsourcing.wsstar.jaxb.notification.base.ObjectFactory.class,
                    com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.ObjectFactory.class);
        } catch (SOAException e) {
            // do nothing
            e.printStackTrace();
        }

        Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
                new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
                new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
                new WsnbModelFactoryImpl());

        topics = new HashMap<String, QName>();
        topics.put(CREATION_TOPIC.getLocalPart(), CREATION_TOPIC);
        topics.put(RAWREPORT_TOPIC.getLocalPart(), RAWREPORT_TOPIC);
    }

    public static SubscribeResponse subscribe(Subscribe subscribe)
            throws UnacceptableInitialTerminationTimeFault, TopicExpressionDialectUnknownFault,
            InvalidTopicExpressionFault, NotifyMessageNotSupportedFault, TopicNotSupportedFault,
            UnsupportedPolicyRequestFault, ResourceUnknownFault, InvalidFilterFault,
            InvalidProducerPropertiesExpressionFault, UnrecognizedPolicyRequestFault,
            InvalidMessageContentExpressionFault, SubscribeCreationFailedFault {

        SubscribeResponse res = new SubscribeResponse();

        org.petalslink.dsb.kernel.pubsub.service.NotificationCenter notificationCenter = org.petalslink.dsb.kernel.pubsub.service.NotificationCenter
                .get();
        if (notificationCenter != null) {
            try {

                Document subscribeDocument = SOAJAXBContext.getInstance().unmarshallAnyElement(
                        subscribe);

                System.out.println("IN subscribe "
                        + XMLHelper.createStringFromDOMDocument(subscribeDocument));

                // got a problem with topic NS: The prefix is added but its NS
                // is not set in the element

                com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe subs = Wsnb4ServUtils
                        .getWsnbReader().readSubscribe(subscribeDocument);

                // FIXME
                // Dirty ACK, add the NS to the subscribe topic since the way we
                // manage it is not standard and Java XML bindings does not
                // support them...

                fixTopic(subs);

                com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse resp = notificationCenter
                        .getManager().getNotificationProducerEngine().subscribe(subs);

                if (resp != null) {
                    Document doc = Wsnb4ServUtils.getWsnbWriter().writeSubscribeResponseAsDOM(resp);

                    res = SOAJAXBContext.getInstance()
                            .marshallAnyType(doc, SubscribeResponse.class);

                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new TopicNotSupportedFault("Technical Fault", e);
            }
        } else {
            throw new RuntimeException("Can not find the notification center to send subscribe to");
        }

        return res;
    }

    public static UnsubscribeResponse unsubscribe(Unsubscribe unsubscribe)
            throws UnableToDestroySubscriptionFault, ResourceUnknownFault {
        UnsubscribeResponse res = null;

        org.petalslink.dsb.kernel.pubsub.service.NotificationCenter notificationCenter = org.petalslink.dsb.kernel.pubsub.service.NotificationCenter
                .get();
        if (notificationCenter != null) {

            try {

                Document in = SOAJAXBContext.getInstance().unmarshallAnyElement(unsubscribe);

                com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Unsubscribe u = Wsnb4ServUtils
                        .getWsnbReader().readUnsubscribe(in);

                com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.UnsubscribeResponse resp = notificationCenter
                        .getManager().getSubscriptionManagerEngine().unsubscribe(u);

                if (resp != null) {

                    Document doc = Wsnb4ServUtils.getWsnbWriter().writeUnsubscribeResponseAsDOM(
                            resp);

                    res = SOAJAXBContext.getInstance().marshallAnyType(doc,
                            UnsubscribeResponse.class);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException(
                    "Can not find the notification center to send unsubscribe to");
        }
        return res;
    }

    public static void notify(Notify notify) {
        org.petalslink.dsb.kernel.pubsub.service.NotificationCenter notificationCenter = org.petalslink.dsb.kernel.pubsub.service.NotificationCenter
                .get();
        if (notificationCenter != null) {

            try {

                Document in = SOAJAXBContext.getInstance().unmarshallAnyElement(notify);

                System.out
                        .println("In notify request " + XMLHelper.createStringFromDOMDocument(in));

                com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify n = Wsnb4ServUtils
                        .getWsnbReader().readNotify(in);
                
                fixTopic(n);

                notificationCenter.getSender().notify(n);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Can not find the notification center to send notify to");
        }
    }

    public static GetResourcePropertyResponse getResourceProperty(QName name)
            throws ResourceUnavailableFault, InvalidResourcePropertyQNameFault,
            ResourceUnknownFault {
        GetResourcePropertyResponse res = null;

        org.petalslink.dsb.kernel.pubsub.service.NotificationCenter notificationCenter = org.petalslink.dsb.kernel.pubsub.service.NotificationCenter
                .get();
        if (notificationCenter != null) {

            try {
                com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse resp = notificationCenter
                        .getManager().getNotificationProducerEngine().getResourceProperty(name);
                if (resp != null) {
                    Document doc = Wsnb4ServUtils.getWsrfrpWriter()
                            .writeGetResourcePropertyResponseAsDOM(resp);

                    res = SOAJAXBContext.getInstance().marshallAnyType(doc,
                            GetResourcePropertyResponse.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException(
                    "Can not find the notification center to send getResourceProperty to");
        }
        return res;
    }

    protected static final String getPrefix(String topicContent) {
        if (topicContent == null || topicContent.length() == 0) {
            return "";
        }

        if (topicContent.indexOf(':') == -1) {
            return "";
        }

        return topicContent.substring(0, topicContent.indexOf(':'));
    }

    protected static final String getLocalPart(String topicContent) {
        if (topicContent == null || topicContent.length() == 0) {
            return "";
        }

        if (topicContent.indexOf(':') == -1) {
            return topicContent;
        }

        return topicContent.substring(topicContent.indexOf(':') + 1);
    }

    protected static void fixTopic(
            com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe subs) {
        com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.TopicExpressionType topic = subs
                .getFilter().getTopicExpressions().get(0);

        if (topic != null) {
            String content = topic.getContent();
            String localPart = getLocalPart(content);

            QName topicQName = topics.get(localPart);
            if (topicQName != null) {
                topic.setContent(topicQName.getPrefix() + ":" + topicQName.getLocalPart());
                topic.addTopicNamespace(topicQName.getPrefix(),
                        URI.create(topicQName.getNamespaceURI()));
            }
        }
    }

    protected static void fixTopic(
            com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify notify) {
        if (notify == null || notify.getNotificationMessage() == null
                || notify.getNotificationMessage().size() == 0) {
            return;
        }

        List<NotificationMessageHolderType> messages = notify.getNotificationMessage();
        com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.TopicExpressionType topic = messages
                .get(0).getTopic();

        if (topic != null) {
            String content = topic.getContent();
            String localPart = getLocalPart(content);

            QName topicQName = topics.get(localPart);
            if (topicQName != null) {
                topic.setContent(topicQName.getPrefix() + ":" + topicQName.getLocalPart());
                topic.addTopicNamespace(topicQName.getPrefix(),
                        URI.create(topicQName.getNamespaceURI()));
            }
        }
    }
}
