/**
 * 
 */
package org.petalslink.dsb.notification.commons;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.petalslink.dsb.notification.commons.api.NotificationManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ebmwebsourcing.easycommons.xml.XMLPrettyPrinter;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.WstopConstants;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.abstraction.TopicNamespaceType;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.abstraction.TopicSetType;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.abstraction.TopicType;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.refinedabstraction.RefinedWstopFactory;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.utils.WstopException;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.TopicTypeImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.engines.NotificationProducerEngine;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.engines.SubscriptionManagerEngine;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.topic.TopicsManagerEngine;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.topic.WstConstants;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * The default implementation of the notification manager.
 * 
 * @author chamerling
 * 
 */
public class NotificationManagerImpl implements NotificationManager {

    public static final String EBM_RESOURCEIDS_PREFIX = "ebm";

    public static final String EBM_RESOURCEIDS_NAMESPACE_URI = "http://petals.ow2.org/ebmwebsourcing/specific/ResourceIds";

    private static final String EBM_TOPICS_EXTENSION_NAMESPACE_URI = "http://org.ow2.petals/ebmwebsourcing/specific/topicExtension";

    public static final QName SUPPORTED_QNAME_ATTR = new QName(EBM_TOPICS_EXTENSION_NAMESPACE_URI,
            "supported", EBM_RESOURCEIDS_PREFIX);

    private static Logger logger = Logger.getLogger(NotificationManagerImpl.class.getName());

    private TopicNamespaceType topicNamespace;

    private TopicsManagerEngine topicsManagerEngine;

    private SubscriptionManagerEngine subscriptionManagerEngine;

    private NotificationProducerEngine notificationProducerEngine;

    private QName serviceName;

    private QName interfaceName;

    private String endpointName;

    private List<String> supportedTopics;

    static {
        Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
                new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
                new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
                new WsnbModelFactoryImpl());
    }

    /**
     * 
     * @param topicSet
     * @param topicNamespaceRPUpdate
     * @param serviceName
     * @param interfaceName
     * @param endpointName
     */
    public NotificationManagerImpl(Document topicSet, Document topicNamespaceRPUpdate,
            QName serviceName, QName interfaceName, String endpointName) {
        init(topicSet, topicNamespaceRPUpdate, serviceName, interfaceName, endpointName);
    }

    /**
     * Build the manager using URLs for topic resources
     * 
     * @param topicSetXML
     * @param topicNamespaceRPUpdate
     * @param serviceName
     * @param interfaceName
     * @param endpointName
     */
    public NotificationManagerImpl(URL topicSetXML, URL topicNamespaceRPUpdate, QName serviceName,
            QName interfaceName, String endpointName) {

        Document topicSetDom = null;
        Document topicNSRPUpdateDom = null;
        try {
            topicSetDom = SOAUtil.getInstance().getDocumentBuilderFactory().newDocumentBuilder()
                    .parse(topicSetXML.openStream());

            topicNSRPUpdateDom = SOAUtil.getInstance().getDocumentBuilderFactory()
                    .newDocumentBuilder().parse(topicNamespaceRPUpdate.openStream());

            init(topicSetDom, topicNSRPUpdateDom, serviceName, interfaceName, endpointName);

        } catch (Exception e) {
            logger.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create all the needed resources
     * 
     * @param topicSet
     * @param topicNamespaceRPUpdate
     * @param serviceName
     * @param interfaceName
     * @param endpointName
     */
    protected void init(Document topicSet, Document topicNamespaceRPUpdate, QName serviceName,
            QName interfaceName, String endpointName) {
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Intializing the notification manager");
        }

        this.endpointName = endpointName;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
        
        TopicSetType topicSetType = null;
        
        try {
            topicSetType = RefinedWstopFactory.getInstance().getWstopReader()
                    .readTopicSetType(topicSet);

            this.topicNamespace = RefinedWstopFactory.getInstance().getWstopReader()
                    .readTopicNamespaceType(topicNamespaceRPUpdate);

            if (logger.isLoggable(Level.INFO)) {
                final org.w3c.dom.Document out = RefinedWstopFactory.getInstance().getWstopWriter()
                        .writeTopicSetTypeAsDOM(topicSetType);
                final org.w3c.dom.Document out2 = RefinedWstopFactory.getInstance().getWstopWriter()
                        .writeTopicNamespaceTypeAsDOM(this.topicNamespace);
                logger.info("TopicSet : " + XMLPrettyPrinter.prettyPrint(out));
                logger.info("Topic NS RP Update : " + XMLPrettyPrinter.prettyPrint(out2));
            }
        } catch (WstopException e1) {
            e1.printStackTrace();
        }

        this.topicsManagerEngine = new TopicsManagerEngine();
        this.subscriptionManagerEngine = new SubscriptionManagerEngine(logger);

        this.subscriptionManagerEngine.setSubscriptionsManagerEdp(getEndpointName());
        this.subscriptionManagerEngine.setSubscriptionsManagerInterface(getInterfaceName());
        this.subscriptionManagerEngine.setSubscriptionsManagerService(getServiceName());

        // TODO : move to to a dedicated component?
        try {
            this.notificationProducerEngine = new NotificationProducerEngine(logger,
                    getTopicsManagerEngine(), getSubscriptionManagerEngine(), false, topicSetType,
                    getTopicNamespace(), "wsn", null);
        } catch (WsnbException e) {
            e.printStackTrace();
        }
    }

    /**
	 * @deprecated
	 */
    public NotificationManagerImpl(URL topicNamespaces, List<String> supportedTopics,
            QName serviceName, QName interfaceName, String endpointName) {
        this.endpointName = endpointName;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;

        Document docTopicNs = null;
        TopicSetType topicSetType = null;
        try {
            docTopicNs = SOAUtil.getInstance().getDocumentBuilderFactory().newDocumentBuilder()
                    .parse(topicNamespaces.openStream());

            this.supportedTopics = supportedTopics;
            this.topicNamespace = RefinedWstopFactory.getInstance().getWstopReader()
                    .readTopicNamespaceType(docTopicNs);
            topicSetType = this.createTopicSetFromTopicNamespace(topicNamespace, supportedTopics);

            final org.w3c.dom.Document topicSetDom = RefinedWstopFactory.getInstance()
                    .getWstopWriter().writeTopicSetTypeAsDOM(topicSetType);

            System.out.println("topicSetDom = " + XMLPrettyPrinter.prettyPrint(topicSetDom));

        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (WstopException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotificationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.topicsManagerEngine = new TopicsManagerEngine();
        this.subscriptionManagerEngine = new SubscriptionManagerEngine(logger);

        this.subscriptionManagerEngine.setSubscriptionsManagerEdp(getEndpointName());
        this.subscriptionManagerEngine.setSubscriptionsManagerInterface(getInterfaceName());
        this.subscriptionManagerEngine.setSubscriptionsManagerService(getServiceName());

        // TODO : move to to a dedicated component?
        try {
            this.notificationProducerEngine = new NotificationProducerEngine(logger,
                    getTopicsManagerEngine(), getSubscriptionManagerEngine(), true, topicSetType,
                    getTopicNamespace(), "wsn", null);
        } catch (WsnbException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    private QName getServiceName() {
        return this.serviceName;
    }

    /**
     * @return
     */
    private QName getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * @return
     */
    private String getEndpointName() {
        return this.endpointName;
    }

    public TopicSetType createTopicSetFromTopicNamespace(final TopicNamespaceType topicns,
            final List<String> topics) throws NotificationException {
        TopicSetType res = null;

        TopicNamespaceType topicNS = null;

        org.w3c.dom.Document domDocument = null;
        try {
            domDocument = RefinedWstopFactory.getInstance().getWstopWriter()
                    .writeTopicNamespaceTypeAsDOM(topicns);
        } catch (WstopException e) {
            throw new NotificationException(e);
        }

        // convert dom to jdom
        final DOMBuilder builder = new DOMBuilder();
        final org.jdom.Document jdomDocument = builder.build(domDocument);

        this.addSupportedTopicAttr(jdomDocument.getRootElement().getChildren(), topics);

        // convert jdom to dom
        final DOMOutputter converter = new DOMOutputter();
        try {
            domDocument = converter.output(jdomDocument);
        } catch (final JDOMException e) {
            throw new NotificationException(e);
        }

        // convert dom to topicSet
        try {
            topicNS = RefinedWstopFactory.getInstance().getWstopReader()
                    .readTopicNamespaceType(domDocument);
        } catch (WstopException e) {
            throw new NotificationException(e);
        }

        res = this.createTopicSetFromSupportedTopicNamespace(topicNS);

        return res;
    }

    public TopicSetType createTopicSetFromSupportedTopicNamespace(final TopicNamespaceType topicns)
            throws NotificationException {
        TopicSetType res = null;

        final Namespace tns = Namespace.getNamespace("tns", topicns.getNamespace().toString());
        final org.jdom.Element root = this.createEmptyTopicSet();
        final org.jdom.Document doc = new org.jdom.Document(root);
        this.createTopicSetTree((List) topicns.getTopics(), root, tns, true);

        // convert jdom to dom
        org.w3c.dom.Document domDocument = null;
        final DOMOutputter converter = new DOMOutputter();
        try {
            domDocument = converter.output(doc);
        } catch (final JDOMException e) {
            throw new NotificationException(e);
        }

        // convert dom to topicSet
        try {
            res = RefinedWstopFactory.getInstance().getWstopReader().readTopicSetType(domDocument);
        } catch (WstopException e) {
            throw new NotificationException(e);
        }

        return res;
    }

    private org.jdom.Element createEmptyTopicSet() {
        final Namespace wstop = Namespace.getNamespace(WstConstants.PREFIX,
                WstConstants.NAMESPACE_URI);
        final Namespace xsi = Namespace.getNamespace(WstConstants.XML_SCHEMA_PREFIX,
                WstConstants.XML_SCHEMA_NAMESPACE);

        final org.jdom.Element root = new org.jdom.Element(
                WstConstants.TOPIC_SET_QNAME.getLocalPart(), wstop);
        root.addNamespaceDeclaration(xsi);
        root.setAttribute("schemaLocation",
                "http://docs.oasis-open.org/wsn/t-1 http://docs.oasis-open.org/wsn/t-1.xsd", xsi);
        return root;
    }

    private void addSupportedTopicAttr(final List<org.jdom.Element> children,
            final List<String> topics) {
        for (final org.jdom.Element child : children) {
            if (child.getName().equals(WstopConstants.TOPIC_QNAME.getLocalPart())
                    && child.getNamespaceURI().equals(WstopConstants.TOPIC_QNAME.getNamespaceURI())) {
                if (topics.contains(child.getAttribute("name").getValue())) {
                    child.setAttribute(SUPPORTED_QNAME_ATTR.getLocalPart(), "true", Namespace
                            .getNamespace(SUPPORTED_QNAME_ATTR.getPrefix(),
                                    SUPPORTED_QNAME_ATTR.getNamespaceURI()));
                }
                if (child.getChildren() != null && child.getChildren().size() > 0) {
                    this.addSupportedTopicAttr(child.getChildren(), topics);
                }
            }
        }
    }

    private void createTopicSetTree(final List<TopicType> topics, final org.jdom.Element root,
            final Namespace tns, final boolean first) {

        final Namespace wstop = Namespace.getNamespace("wstop",
                "http://docs.oasis-open.org/wsn/t-1");

        for (final TopicType topic : topics) {
            org.jdom.Element childTopic = null;
            if (first) {
                childTopic = new org.jdom.Element(topic.getName(), tns);
            } else {
                childTopic = new org.jdom.Element(topic.getName());
            }
            if (isTopicSupported(topic) != null && isTopicSupported(topic) == true) {
                childTopic.setAttribute("topic", "true", wstop);
                root.addContent(childTopic);
            }
            if (topic.getTopics() != null && topic.getTopics().size() > 0) {
                this.createTopicSetTree(topic.getTopics(), childTopic, tns, false);
            }
        }
    }

    public static Boolean isTopicSupported(TopicType topic) {
        Boolean res = null;
        com.ebmwebsourcing.wsstar.jaxb.notification.topics.TopicType model = TopicTypeImpl
                .toJaxbModel(topic);
        final String value = model.getOtherAttributes().get(SUPPORTED_QNAME_ATTR);
        if (value != null) {
            res = Boolean.valueOf(value);
        }
        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petalslink.dsb.notification.commons.NotificationManager#getTopicNamespace
     * ()
     */
    public TopicNamespaceType getTopicNamespace() {
        return topicNamespace;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petalslink.dsb.notification.commons.NotificationManager#getTopicSet()
     */
    public TopicSetType getTopicSet() {
    	return this.notificationProducerEngine.getActorAsRP().getTopicSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.notification.commons.NotificationManager#
     * getTopicsManagerEngine()
     */
    public TopicsManagerEngine getTopicsManagerEngine() {
        return topicsManagerEngine;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.notification.commons.NotificationManager#
     * getSubscriptionManagerEngine()
     */
    public SubscriptionManagerEngine getSubscriptionManagerEngine() {
        return subscriptionManagerEngine;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.notification.commons.NotificationManager#
     * getNotificationProducerEngine()
     */
    public NotificationProducerEngine getNotificationProducerEngine() {
        return notificationProducerEngine;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.notification.commons.api.NotificationManager#
     * getSupportedTopics()
     */
    public List<String> getSupportedTopics() {
        return this.supportedTopics;
    }

}
