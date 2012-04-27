/**
 * 
 */
package org.petalslink.dsb.kernel.esmanagement;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe;
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
public class NotifyProxyTest extends TestCase {

    static {
        Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
                new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
                new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
                new WsnbModelFactoryImpl());
    }

    public void testGetPrefix() throws Exception {
        String topicContent = "dsb:Foo";
        String prefix = NotificationProxy.getPrefix(topicContent);
        assertEquals("dsb", prefix);
    }

    public void testGetNoPrefix() throws Exception {
        String topicContent = "Foo";
        String prefix = NotificationProxy.getPrefix(topicContent);
        assertEquals("", prefix);
    }

    public void testGetPrefixFromNull() throws Exception {
        String prefix = NotificationProxy.getPrefix(null);
        assertEquals("", prefix);
    }

    public void testGetLocalPart() throws Exception {
        String topicContent = "dsb:Foo";
        String part = NotificationProxy.getLocalPart(topicContent);
        assertEquals("Foo", part);
    }

    public void testGetNoLocalPart() throws Exception {
        String topicContent = "Foo";
        String part = NotificationProxy.getLocalPart(topicContent);
        assertEquals("Foo", part);
    }

    public void testGetNullLocalPart() throws Exception {
        String prefix = NotificationProxy.getLocalPart(null);
        assertEquals("", prefix);
    }

    /**
     * Check that all is OK with a good message
     * 
     * @throws Exception
     */
    public void testFixSubscribeNSOK() throws Exception {
        Document payload = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            payload = dbf.newDocumentBuilder().parse(
                    NotifyProxyTest.class.getResourceAsStream("/subscribe-ok.xml"));
            System.out.println(XMLHelper.createStringFromDOMDocument(payload));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Subscribe subscribe = RefinedWsnbFactory.getInstance().getWsnbReader()
                .readSubscribe(payload);

        String topicContent = subscribe.getFilter().getTopicExpressions().get(0).getContent();
        List<QName> ns = subscribe.getFilter().getTopicExpressions().get(0).getTopicNamespaces();
        QName uri = ns.get(0);

        NotificationProxy.fixTopic(subscribe);

        // check that all is the same
        assertEquals(topicContent, subscribe.getFilter().getTopicExpressions().get(0).getContent());
        assertEquals(uri, subscribe.getFilter().getTopicExpressions().get(0).getTopicNamespaces()
                .get(0));
    }

    public void testFixSubscribeNoNS() throws Exception {

        Document payload = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            payload = dbf.newDocumentBuilder().parse(
                    NotifyProxyTest.class.getResourceAsStream("/subscribe-nouri.xml"));
            System.out.println(XMLHelper.createStringFromDOMDocument(payload));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Subscribe subscribe = RefinedWsnbFactory.getInstance().getWsnbReader()
                .readSubscribe(payload);

        String topicContent = subscribe.getFilter().getTopicExpressions().get(0).getContent();
        List<QName> ns = subscribe.getFilter().getTopicExpressions().get(0).getTopicNamespaces();

        assertTrue(ns == null || ns.size() == 0);

        NotificationProxy.fixTopic(subscribe);

        // check that all is the same
        assertEquals(topicContent, subscribe.getFilter().getTopicExpressions().get(0).getContent());
        ns = subscribe.getFilter().getTopicExpressions().get(0).getTopicNamespaces();

        assertTrue(ns.size() == 1);
        System.out.println(ns);

        for (QName qName : ns) {
            System.out.println(qName);
        }

        String prefix = subscribe.getFilter().getTopicExpressions().get(0).getTopicNamespaces()
                .get(0).getLocalPart();

        System.out.println(prefix);

        System.out.println(XMLHelper.createStringFromDOMDocument(RefinedWsnbFactory.getInstance()
                .getWsnbWriter().writeSubscribeAsDOM(subscribe)));

        assertEquals(NotificationProxy.RAWREPORT_TOPIC.getPrefix(), prefix);
        assertEquals(NotificationProxy.RAWREPORT_TOPIC.getNamespaceURI(), subscribe.getFilter()
                .getTopicExpressions().get(0).getTopicNamespaces().get(0).getNamespaceURI());

    }

    public void testFixNotifyNSOK() throws Exception {
        Document payload = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            payload = dbf.newDocumentBuilder().parse(
                    NotifyProxyTest.class.getResourceAsStream("/notify-ok.xml"));
            System.out.println(XMLHelper.createStringFromDOMDocument(payload));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Notify notify = RefinedWsnbFactory.getInstance().getWsnbReader().readNotify(payload);

        String topicContent = notify.getNotificationMessage().get(0).getTopic().getContent();
        List<QName> ns = notify.getNotificationMessage().get(0).getTopic().getTopicNamespaces();
        QName uri = ns.get(0);

        NotificationProxy.fixTopic(notify);

        // check that all is the same
        assertEquals(topicContent, notify.getNotificationMessage().get(0).getTopic().getContent());
        assertEquals(uri, notify.getNotificationMessage().get(0).getTopic().getTopicNamespaces()
                .get(0));
    }

    public void testFixNotifyNoNS() throws Exception {

        Document payload = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            payload = dbf.newDocumentBuilder().parse(
                    NotifyProxyTest.class.getResourceAsStream("/notify-nouri.xml"));
            System.out.println(XMLHelper.createStringFromDOMDocument(payload));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Notify notify = RefinedWsnbFactory.getInstance().getWsnbReader().readNotify(payload);

        String topicContent = notify.getNotificationMessage().get(0).getTopic().getContent();
        List<QName> ns = notify.getNotificationMessage().get(0).getTopic().getTopicNamespaces();

        assertTrue(ns == null || ns.size() == 0);

        NotificationProxy.fixTopic(notify);

        // check that all is the same
        assertEquals(topicContent, notify.getNotificationMessage().get(0).getTopic().getContent());
        ns = notify.getNotificationMessage().get(0).getTopic().getTopicNamespaces();

        assertTrue(ns.size() == 1);
        System.out.println(ns);

        for (QName qName : ns) {
            System.out.println(qName);
        }

        String prefix = notify.getNotificationMessage().get(0).getTopic().getTopicNamespaces()
                .get(0).getLocalPart();

        System.out.println(prefix);

        System.out.println(XMLHelper.createStringFromDOMDocument(RefinedWsnbFactory.getInstance()
                .getWsnbWriter().writeNotifyAsDOM(notify)));

        assertEquals(NotificationProxy.RAWREPORT_TOPIC.getPrefix(), prefix);
        assertEquals(NotificationProxy.RAWREPORT_TOPIC.getNamespaceURI(), notify
                .getNotificationMessage().get(0).getTopic().getTopicNamespaces().get(0)
                .getNamespaceURI());

    }
}
