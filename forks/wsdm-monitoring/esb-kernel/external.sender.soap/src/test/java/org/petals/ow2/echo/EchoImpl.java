
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package org.petals.ow2.echo;

import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.1.4
 * Wed Jul 29 10:41:28 CEST 2009
 * Generated source version: 2.1.4
 * 
 */

@javax.jws.WebService(
                      serviceName = "echo",
                      portName = "echoSOAP",
                      targetNamespace = "http://ow2.petals.org/echo/",
                      wsdlLocation = "./src/test/resources/wsdl/echo.wsdl",
                      endpointInterface = "org.petals.ow2.echo.Echo")
                      
public class EchoImpl implements Echo {

    private static final Logger LOG = Logger.getLogger(EchoImpl.class.getName());

    /* (non-Javadoc)
     * @see org.petals.ow2.echo.Echo#echo(java.lang.String  in )*
     */
    public java.lang.String echo(java.lang.String in) { 
        LOG.info("Executing operation echo");
        System.out.println(in);
        try {
            java.lang.String _return = in;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
