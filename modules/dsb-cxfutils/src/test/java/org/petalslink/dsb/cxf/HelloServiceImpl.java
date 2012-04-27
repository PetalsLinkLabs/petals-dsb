/**
 * 
 */
package org.petalslink.dsb.cxf;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * @author chamerling
 *
 */
public class HelloServiceImpl implements HelloService {
    
    @Resource
    WebServiceContext wsContext;

    /* (non-Javadoc)
     * @see org.petalslink.dsb.cxf.HelloService#sayHello(java.lang.String)
     */
    public String sayHello(String in) {
        System.out.println("WS Called");
        
        MessageContext message = wsContext.getMessageContext();
        for (String key : message.keySet()) {
            System.out.println(key + " : " + message.get(key));
            
        };
        
        System.out.println(wsContext.getMessageContext());
        
        return in;
    }

}
