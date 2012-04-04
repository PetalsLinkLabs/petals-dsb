/**
 * 
 */
package org.petalsling.dsb.integration.esmanagement;

import java.util.List;

import javax.xml.bind.JAXBElement;

import junit.framework.TestCase;

import org.petalsling.dsb.integration.esmanagement.services.ESManagementBindService;
import org.petalsling.dsb.integration.esmanagement.services.ESManagementProxifyService;
import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;

import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbResourceIdentifier;
import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbWsdl;
import esstar.petalslink.com.data.management.admin._1.GetResourceIdentifiers;
import esstar.petalslink.com.data.management.admin._1.GetResourceIdentifiersResponse;
import esstar.petalslink.com.data.management.user._1.Bind;
import esstar.petalslink.com.data.management.user._1.BindResponse;
import esstar.petalslink.com.data.management.user._1.Proxify;
import esstar.petalslink.com.data.management.user._1.ProxifyResponse;
import esstar.petalslink.com.service.management.admin._1_0.AdminManagement;
import esstar.petalslink.com.service.management.user._1_0.UserManagement;

/**
 * @author chamerling
 * 
 */
public class ServiceManagementTest extends TestCase {

    public static final String baseURL = "http://localhost:7600/petals/ws";

    /**
     * @param name
     */
    public ServiceManagementTest(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBindService() throws Exception {
        UserManagement client = CXFHelper.getClient(baseURL, UserManagement.class);
        AdminManagement admin = CXFHelper.getClient(baseURL, AdminManagement.class);

        Service s = null;
        // create a local web service to bind...
        String serviceURL = "http://localhost:9987/ws/integration/esmanagement/ESManagementBindService";
        s = CXFHelper.getServiceFromFinalURL(serviceURL, ESManagementBindService.class,
                new ESManagementBindService() {
                    public String foo(String bar) {
                        System.out.println("Got a call");
                        return bar.toUpperCase();
                    }
                });

        try {
            System.out.println("Creating service to bind...");
            s.start();
            System.out.println("Created!");

            GetResourceIdentifiersResponse ids = admin
                    .getResourceIdentifiers(new GetResourceIdentifiers());
            List<EJaxbResourceIdentifier> list = ids.getResourceIdentifier();
            System.out.println("Initial identifiers before bind:");
            for (EJaxbResourceIdentifier eJaxbResourceIdentifier : list) {
                System.out.println(eJaxbResourceIdentifier.getResourceType() + " : "
                        + eJaxbResourceIdentifier.getId());
            }

            System.out.println("Binding...");
            Bind bind = new Bind();
            easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory factory = new easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory();
            JAXBElement<String> url = factory.createEJaxbWsdlUrl(serviceURL + "?wsdl");
            EJaxbWsdl wsdl = new EJaxbWsdl();
            wsdl.setUrl(url);
            bind.setWsdl(wsdl);
            BindResponse response = client.bind(bind);
            System.out.println("Bound!");

            System.out.println("Bind response:" + response.getEndpointName());

            System.out.println();
            GetResourceIdentifiersResponse idsAfter = admin
                    .getResourceIdentifiers(new GetResourceIdentifiers());
            List<EJaxbResourceIdentifier> listAfter = idsAfter.getResourceIdentifier();
            System.out.println("Initial identifiers after bind:");
            for (EJaxbResourceIdentifier eJaxbResourceIdentifier : listAfter) {
                System.out.println(eJaxbResourceIdentifier.getResourceType() + " : "
                        + eJaxbResourceIdentifier.getId());
            }

            // check that the bound service is available in the resources IDs as
            // bound service...
            
            // EP : ESManagementBindServicePort
            // SRV : {http://services.esmanagement.integration.dsb.petalsling.org/}ESManagementBindServiceService
            // ITF : {http://services.esmanagement.integration.dsb.petalsling.org/}ESManagementBindService
            // we should have an ID which reflect the newly bound service...

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            fail();
        } finally {
            if (s != null) {
                s.stop();
            }
        }
    }

    public void testProxy() throws Exception {
        UserManagement client = CXFHelper.getClient(baseURL, UserManagement.class);
        AdminManagement admin = CXFHelper.getClient(baseURL, AdminManagement.class);

        Service s = null;
        // create a local web service to bind...
        String serviceURL = "http://localhost:9987/ws/integration/esmanagement/ESManagementProxyService";
        s = CXFHelper.getServiceFromFinalURL(serviceURL, ESManagementProxifyService.class,
                new ESManagementProxifyService() {
                    public String foo(String bar) {
                        System.out.println("Got a call on proxy service");
                        return bar.toUpperCase();
                    }
                });

        try {
            System.out.println("Creating service to proxify...");
            s.start();
            System.out.println("Created!");

            GetResourceIdentifiersResponse ids = admin
                    .getResourceIdentifiers(new GetResourceIdentifiers());
            List<EJaxbResourceIdentifier> list = ids.getResourceIdentifier();
            System.out.println("Initial identifiers before bind:");
            for (EJaxbResourceIdentifier eJaxbResourceIdentifier : list) {
                System.out.println(eJaxbResourceIdentifier.getResourceType() + " : "
                        + eJaxbResourceIdentifier.getId());
            }

            System.out.println("Proxify...");
            Proxify p = new Proxify();
            easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory factory = new easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory();
            JAXBElement<String> url = factory.createEJaxbWsdlUrl(serviceURL + "?wsdl");
            EJaxbWsdl wsdl = new EJaxbWsdl();
            wsdl.setUrl(url);
            p.setWsdl(wsdl);
            ProxifyResponse response = client.proxify(p);
            System.out.println("Proxified!");

            System.out.println("Proxy response:" + response.getExternalAddress());

            GetResourceIdentifiersResponse idsAfter = admin
                    .getResourceIdentifiers(new GetResourceIdentifiers());
            List<EJaxbResourceIdentifier> listAfter = idsAfter.getResourceIdentifier();
            System.out.println("Initial identifiers after proxy:");
            for (EJaxbResourceIdentifier eJaxbResourceIdentifier : listAfter) {
                System.out.println(eJaxbResourceIdentifier.getResourceType() + " : "
                        + eJaxbResourceIdentifier.getId());
            }

            // TODO : Check that the service is available in the bound services
            // in the resources IDs

            System.out.println("Invoking proxified service...");
            System.out.println("TODO");

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            fail();
        } finally {
            if (s != null) {
                s.stop();
            }
        }
    }
}
