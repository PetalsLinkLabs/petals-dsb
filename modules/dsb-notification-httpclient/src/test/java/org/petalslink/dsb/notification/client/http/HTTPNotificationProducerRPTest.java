/**
 * 
 */
package org.petalslink.dsb.notification.client.http;

import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.petalslink.dsb.notification.client.http.simple.HTTPProducerRPClient;

import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
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
public class HTTPNotificationProducerRPTest extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
                new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
                new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
                new WsnbModelFactoryImpl());
    }
    
    public void testGetTopics() throws Exception {
        System.out.println("Creating client");
        HTTPProducerRPClient client = new HTTPProducerRPClient("http://46.105.181.221:8084/petals/services/NotificationConsumerPortService");
        System.out.println("Getting topics");
        List<QName> topics = client.getTopics();
        
        System.out.println("List topics : ");
        for (QName qName : topics) {
            System.out.printf("Prefix '%s', NamespaceURI '%s', Name '%s'", qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart());
            System.out.println();
        }
    }

}
