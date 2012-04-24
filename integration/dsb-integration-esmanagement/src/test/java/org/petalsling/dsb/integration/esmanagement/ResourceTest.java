/**
 * 
 */
package org.petalsling.dsb.integration.esmanagement;

import junit.framework.TestCase;

import org.petalslink.dsb.api.ResourceConstants;
import org.petalslink.dsb.cxf.CXFHelper;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.research.util.SOAException;
import com.ebmwebsourcing.easycommons.research.util.jaxb.SOAJAXBContext;
import com.ebmwebsourcing.easycommons.xml.XMLHelper;

import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbExecutionEnvironmentInformationTypeType;
import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbResourceIdentifier;
import esstar.petalslink.com.data.management.admin._1.GetContent;
import esstar.petalslink.com.data.management.admin._1.GetContentResponse;
import esstar.petalslink.com.data.management.admin._1.GetExecutionEnvironmentInformation;
import esstar.petalslink.com.data.management.admin._1.GetExecutionEnvironmentInformationResponse;
import esstar.petalslink.com.data.management.admin._1.GetResourceIdentifiers;
import esstar.petalslink.com.data.management.admin._1.GetResourceIdentifiersResponse;
import esstar.petalslink.com.service.management.admin._1_0.AdminManagement;

/**
 * @author chamerling
 * 
 */
public class ResourceTest extends TestCase {

    public static final String baseURL = "http://localhost:7600/petals/ws";

    public static final String HELLO_ID = "{http://dsb.petalslink.org/endpoint/identifier/petals-bc-soap/subdomain1/0/}HelloServicePort";

    static {
        try {
            SOAJAXBContext.getInstance().addOtherObjectFactory(
                    easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory.class,
                    com.ebmwebsourcing.wsstar.jaxb.notification.base.ObjectFactory.class,
                    com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.ObjectFactory.class,
                    esstar.petalslink.com.data.management.admin._1.ObjectFactory.class);
        } catch (SOAException e) {
            // do nothing
            e.printStackTrace();
        }
    }

    /**
     * @param name
     */
    public ResourceTest(String name) {
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

    public void testGetResourceIDs() throws Exception {
        AdminManagement client = CXFHelper.getClient(baseURL, AdminManagement.class);
        GetResourceIdentifiersResponse response = client
                .getResourceIdentifiers(new GetResourceIdentifiers());

        for (EJaxbResourceIdentifier identifier : response.getResourceIdentifier()) {
            System.out.println(identifier.getResourceType() + ":" + identifier.getId());
        }

        assertTrue(response.getResourceIdentifier().size() > 0);
    }

    public void testGetResourceContent() throws Exception {
        AdminManagement client = CXFHelper.getClient(baseURL, AdminManagement.class);
        GetContent content = new GetContent();
        EJaxbResourceIdentifier id = new EJaxbResourceIdentifier();
        id.setResourceType(ResourceConstants.ENDPOINT);
        id.setId(HELLO_ID);
        content.setResourceIdentifier(id);

        GetContentResponse response = client.getContent(content);
        assertNotNull(response);

        Document document = SOAJAXBContext.getInstance().unmarshallAnyElement(response);

        String s = XMLHelper.createStringFromDOMDocument(document);
        System.out.println(s);
        
        // Check that it is the right WSDL, must do it with easyWSDL...
        assertNotNull(s);
        assertTrue(s.contains("definitions"));
        assertTrue(s.contains("sayHello"));
        assertTrue(s.contains("HelloServicePort"));
    }

    public void testGetExecEnv() throws Exception {
        AdminManagement client = CXFHelper.getClient(baseURL, AdminManagement.class);
        GetExecutionEnvironmentInformationResponse response = client
                .getExecutionEnvironmentInformation(new GetExecutionEnvironmentInformation());

        assertNotNull(response);

        assertEquals(response.getExecutionEnvironmentInformation().getType(),
                EJaxbExecutionEnvironmentInformationTypeType.ESB);

        // TODO: On a controlled test, we can check all the values...
    }

}
