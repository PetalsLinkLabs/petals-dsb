/**
 * 
 */
package org.petalslink.dsb.jbi;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.ow2.petals.jbi.messaging.endpoint.JBIServiceEndpointImpl;
import org.ow2.petals.kernel.api.service.Location;

/**
 * @author chamerling
 *
 */
public class Adapter {
    
    public static org.petalslink.dsb.api.ServiceEndpoint createServiceEndpoint(
            org.ow2.petals.kernel.api.service.ServiceEndpoint serviceEndpoint) {
        if (serviceEndpoint == null) {
            return null;
        }
        org.petalslink.dsb.api.ServiceEndpoint se = new org.petalslink.dsb.api.ServiceEndpoint();
        if (serviceEndpoint.getLocation() != null) {
            se.setComponentLocation(serviceEndpoint.getLocation().getComponentName());
            se.setSubdomainLocation(serviceEndpoint.getLocation().getSubdomainName());
            se.setContainerLocation(serviceEndpoint.getLocation().getContainerName());
        }
        
        if (serviceEndpoint.getType() != null) {
            se.setType(serviceEndpoint.getType().toString());
        }

        // TODO : change description to string
        // se.setDescription(description);

        se.setEndpointName(serviceEndpoint.getEndpointName());
        se.setServiceName(serviceEndpoint.getServiceName());
        List<QName> itfs = serviceEndpoint.getInterfacesName();
        if (itfs != null) {
            se.setInterfaces(itfs.toArray(new QName[itfs.size()]));
        }
        return se;
    }

    public static JBIServiceEndpointImpl createServiceEndpoint(
            org.petalslink.dsb.api.ServiceEndpoint serviceEndpoint) {
        JBIServiceEndpointImpl se = new JBIServiceEndpointImpl();
        if (serviceEndpoint != null) {
            Location location = new Location(serviceEndpoint.getSubdomainLocation(),
                    serviceEndpoint.getContainerLocation(), serviceEndpoint.getComponentLocation());
            se.setLocation(location);

            se.setStringDescription(serviceEndpoint.getDescription());

            se.setEndpointName(serviceEndpoint.getEndpointName());
            se.setServiceName(serviceEndpoint.getServiceName());
            QName[] itfs = serviceEndpoint.getInterfaces();
            if (itfs != null) {
                List<QName> list = new ArrayList<QName>();
                for (QName qName : itfs) {
                    list.add(qName);
                }
                se.setInterfacesName(list);
            }
        }
        return se;
    }

}