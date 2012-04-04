/**
 * 
 */
package org.petalsling.dsb.integration.esmanagement;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.petalslink.dsb.cxf.CXFHelper;

import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse;

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

    public static final String ENDPOINT_TYPE = "endpoint";

    private static final QName TOPICS = null;

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
        id.setResourceType(ENDPOINT_TYPE);
        id.setId(HELLO_ID);
        content.setResourceIdentifier(id);

        GetContentResponse response = client.getContent(content);
        assertNotNull(response);

        System.out.println(response.getAny());

        assertNotNull(response.getAny());

        // TODO : this is the WSDL as Any, check if it is right...
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

    public void testGetResourceProperty() throws Exception {
        AdminManagement client = CXFHelper.getClient(baseURL, AdminManagement.class);
        com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.GetResourcePropertyResponse response = client
                .getResourceProperty(TOPICS);
        
        assertNotNull(response);
        
    }

}
