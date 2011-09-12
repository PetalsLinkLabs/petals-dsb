/**
 * 
 */
package org.petalslink.dsb.notification.client.http;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.petalslink.dsb.service.client.Client;
import org.petalslink.dsb.service.client.ClientException;
import org.petalslink.dsb.service.client.Message;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.WsnbConstants;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Renew;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.RenewResponse;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Unsubscribe;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.UnsubscribeResponse;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.ISubscriptionManager;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.ebmwebsourcing.wsstar.wsrfbf.services.faults.AbsWSStarFault;

/**
 * @author chamerling
 * 
 */
public class HTTPubscriptionManagerClient implements ISubscriptionManager {

    static Logger logger = Logger.getLogger(HTTPNotificationProducerClient.class.getName());

    private String endpoint;

    /**
     * 
     */
    public HTTPubscriptionManagerClient(String endpoint) {
        this.endpoint = endpoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ebmwebsourcing.wsstar.wsnb.services.ISubscriptionManager#renew(com
     * .ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Renew)
     */
    public RenewResponse renew(Renew renew) throws WsnbException, AbsWSStarFault {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Sending renew message to " + endpoint);
        }

        if (renew == null) {
            throw new WsnbException("renew message can not be null");
        }

        final Document payload = Wsnb4ServUtils.getWsnbWriter().writeRenewAsDOM(renew);

        Client client = new org.petalslink.dsb.service.client.saaj.Client();
        Message response = null;
        try {
            response = client.sendReceive(new Message() {

                public QName getService() {
                    return null;
                }

                public Map<String, String> getProperties() {
                    return null;
                }

                public Document getPayload() {
                    return payload;
                }

                public QName getOperation() {
                    return WsnbConstants.RENEW_QNAME;
                }

                public QName getInterface() {
                    return null;
                }

                public Map<String, Document> getHeaders() {
                    return null;
                }

                public String getEndpoint() {
                    return endpoint;
                }

                public String getProperty(String name) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public void setProperty(String name, String value) {
                    // TODO Auto-generated method stub
                    
                }

                public void setEndpoint(String endpoint) {
                    // TODO Auto-generated method stub
                    
                }

                public void setInterface(QName interfaceQName) {
                    // TODO Auto-generated method stub
                    
                }

                public void setService(QName service) {
                    // TODO Auto-generated method stub
                    
                }

                public void setPayload(Document payload) {
                    // TODO Auto-generated method stub
                    
                }

                public void setOperation(QName operation) {
                    // TODO Auto-generated method stub
                    
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
            throw new WsnbException(e.getMessage());
        }
        if (response == null || response.getPayload() == null) {
            throw new WsnbException("Can not get any response from service");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("renew response : ");
            try {
                logger.fine(XMLHelper.createStringFromDOMDocument(response.getPayload()));
            } catch (TransformerException e) {
            }
        }
        return Wsnb4ServUtils.getWsnbReader().readRenewResponse(response.getPayload());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ebmwebsourcing.wsstar.wsnb.services.ISubscriptionManager#unsubscribe
     * (com
     * .ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Unsubscribe
     * )
     */
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribe) throws WsnbException,
            AbsWSStarFault {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Sending renew message to " + endpoint);
        }

        if (unsubscribe == null) {
            throw new WsnbException("unsubscribe message can not be null");
        }

        final Document payload = Wsnb4ServUtils.getWsnbWriter().writeUnsubscribeAsDOM(unsubscribe);

        Client client = new org.petalslink.dsb.service.client.saaj.Client();
        Message response = null;
        try {
            response = client.sendReceive(new Message() {

                public QName getService() {
                    return null;
                }

                public Map<String, String> getProperties() {
                    return null;
                }

                public Document getPayload() {
                    return payload;
                }

                public QName getOperation() {
                    return WsnbConstants.UNSUBSCRIBE_QNAME;
                }

                public QName getInterface() {
                    return null;
                }

                public Map<String, Document> getHeaders() {
                    return null;
                }

                public String getEndpoint() {
                    return endpoint;
                }

                public String getProperty(String name) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public void setProperty(String name, String value) {
                    // TODO Auto-generated method stub
                    
                }

                public void setEndpoint(String endpoint) {
                    // TODO Auto-generated method stub
                    
                }

                public void setInterface(QName interfaceQName) {
                    // TODO Auto-generated method stub
                    
                }

                public void setService(QName service) {
                    // TODO Auto-generated method stub
                    
                }

                public void setPayload(Document payload) {
                    // TODO Auto-generated method stub
                    
                }

                public void setOperation(QName operation) {
                    // TODO Auto-generated method stub
                    
                }
            });
        } catch (ClientException e) {
            e.printStackTrace();
            throw new WsnbException(e.getMessage());
        }
        if (response == null || response.getPayload() == null) {
            throw new WsnbException("Can not get any response from service");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("renew response : ");
            try {
                logger.fine(XMLHelper.createStringFromDOMDocument(response.getPayload()));
            } catch (TransformerException e) {
            }
        }
        return Wsnb4ServUtils.getWsnbReader().readUnsubscribeResponse(response.getPayload());
    }

}
