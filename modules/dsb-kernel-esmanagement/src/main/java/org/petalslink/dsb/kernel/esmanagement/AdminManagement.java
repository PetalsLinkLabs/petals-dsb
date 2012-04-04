/**
 * 
 */
package org.petalslink.dsb.kernel.esmanagement;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rpw_2.InvalidResourcePropertyQNameFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnavailableFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.objectweb.fractal.fraclet.annotation.annotations.FractalComponent;
import org.objectweb.fractal.fraclet.annotation.annotations.Interface;
import org.objectweb.fractal.fraclet.annotation.annotations.LifeCycle;
import org.objectweb.fractal.fraclet.annotation.annotations.Monolog;
import org.objectweb.fractal.fraclet.annotation.annotations.Provides;
import org.objectweb.fractal.fraclet.annotation.annotations.Requires;
import org.objectweb.fractal.fraclet.annotation.annotations.type.Contingency;
import org.objectweb.fractal.fraclet.annotation.annotations.type.LifeCycleType;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.WSDL4ComplexWsdlFactory;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.api.Description;
import org.ow2.easywsdl.extensions.wsdl4complexwsdl.api.WSDL4ComplexWsdlException;
import org.ow2.petals.jbi.messaging.registry.EndpointRegistry;
import org.ow2.petals.jbi.messaging.registry.RegistryException;
import org.ow2.petals.kernel.configuration.ConfigurationService;
import org.ow2.petals.util.oldies.LoggingUtil;
import org.petalslink.dsb.api.ServiceEndpoint;
import org.petalslink.dsb.jbi.Adapter;
import org.petalslink.dsb.kernel.api.DSBConfigurationService;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.SOAPServiceExposer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Notify;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Subscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.SubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Unsubscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.UnsubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.GetResourcePropertyResponse;

import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbExecutionEnvironmentInformation;
import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbExecutionEnvironmentInformationTypeType;
import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbInterfaceConnectorType;
import easybox.esstar.petalslink.com.management.model.datatype._1.EJaxbResourceIdentifier;
import esstar.petalslink.com.data.management.admin._1.Deploy;
import esstar.petalslink.com.data.management.admin._1.DeployResponse;
import esstar.petalslink.com.data.management.admin._1.GetAdditionalContent;
import esstar.petalslink.com.data.management.admin._1.GetAdditionalContentResponse;
import esstar.petalslink.com.data.management.admin._1.GetContent;
import esstar.petalslink.com.data.management.admin._1.GetContentResponse;
import esstar.petalslink.com.data.management.admin._1.GetExecutionEnvironmentInformation;
import esstar.petalslink.com.data.management.admin._1.GetExecutionEnvironmentInformationResponse;
import esstar.petalslink.com.data.management.admin._1.GetResourceIdentifiers;
import esstar.petalslink.com.data.management.admin._1.GetResourceIdentifiersResponse;
import esstar.petalslink.com.service.management.admin._1_0.AdminManagementException;

/**
 * CF com.petalslink.easyresources.execution_environment_connection_api._1_0.
 * ExecutionEnvironmentManager for Resource management
 * 
 * @author chamerling
 * 
 */
@WebService
@FractalComponent
@Provides(interfaces = { @Interface(name = "service", signature = esstar.petalslink.com.service.management.admin._1_0.AdminManagement.class) })
public class AdminManagement implements
        esstar.petalslink.com.service.management.admin._1_0.AdminManagement {

    @Requires(name = "endpoint", signature = EndpointRegistry.class)
    private EndpointRegistry endpointRegistry;

    @Requires(name = "configuration", signature = ConfigurationService.class)
    private ConfigurationService configurationService;

    @Requires(name = "dsbconfiguration", signature = DSBConfigurationService.class)
    private DSBConfigurationService dsbConfigurationService;

    @Requires(name = "soapbinder", signature = SOAPServiceBinder.class, contingency = Contingency.OPTIONAL)
    private SOAPServiceBinder binder;

    @Requires(name = "soapexposer", signature = SOAPServiceExposer.class, contingency = Contingency.OPTIONAL)
    private SOAPServiceExposer exposer;

    @Monolog(name = "logger")
    private Logger logger;

    private LoggingUtil log;

    @LifeCycle(on = LifeCycleType.START)
    protected void start() {
        this.log = new LoggingUtil(this.logger);
    }

    @LifeCycle(on = LifeCycleType.STOP)
    protected void stopFc() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * connectToGovernance(java.lang.String)
     */
    public void connectToGovernance(String arg0) throws AdminManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Connect to governance %s", arg0);
        }
        if (arg0 == null) {
            throw new AdminManagementException("Null URL is not allowed");
        }

        throw new AdminManagementException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.admin._1_0.AdminManagement#deploy
     * (esstar.petalslink.com.data.management.admin._1.Deploy)
     */
    public DeployResponse deploy(Deploy deploy) throws AdminManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Deploy artifact %s", deploy.getArtifact().getFileURI());
        }
        if (deploy == null) {
            throw new AdminManagementException("Null deploy message not allowed");
        }
        throw new AdminManagementException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * getAdditionalContent
     * (esstar.petalslink.com.data.management.admin._1.GetAdditionalContent)
     */
    public GetAdditionalContentResponse getAdditionalContent(
            GetAdditionalContent getAdditionalContent) throws AdminManagementException {
        GetAdditionalContentResponse response = new GetAdditionalContentResponse();

        ServiceEndpoint se = getEndpoint(getAdditionalContent.getResourceIdentifier().getId());
        if (se == null) {
            throw new AdminManagementException(String.format(
                    "Impossible to find endpoint corresponding to this qname: %s",
                    getAdditionalContent.getResourceIdentifier().getId()));
        }

        String relativePath = getAdditionalContent.getId();
        Document doc = getDescription(se.getDescription());

        Document importDesc = getImport(doc, relativePath);
        if (importDesc == null) {
            throw new AdminManagementException(String.format(
                    "Impossible to find import corresponding to %s on endpoint %s", relativePath,
                    getAdditionalContent.getResourceIdentifier().getId()));
        }
        response.setAny(importDesc.getDocumentElement());
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * getContent(esstar.petalslink.com.data.management.admin._1.GetContent)
     */
    public GetContentResponse getContent(GetContent getContent) throws AdminManagementException {
        GetContentResponse response = new GetContentResponse();

        // only get the WSDLs for now...

        // get the endpoint from its ID
        ServiceEndpoint se = getEndpoint(getContent.getResourceIdentifier().getId());
        if (se == null) {
            throw new AdminManagementException(String.format(
                    "Impossible to find endpoint corresponding to this qname: %s", getContent
                            .getResourceIdentifier().getId()));
        }

        String description = se.getDescription();
        if (description == null) {
            throw new AdminManagementException(String.format(
                    "No service description found for endpoint: %s", getContent
                            .getResourceIdentifier().getId()));
        }

        Document doc = getDescription(se.getDescription());
        if (doc == null) {
            throw new AdminManagementException(
                    "Can not generate WSDL document from endpoint description");
        }
        response.setAny(doc.getDocumentElement());
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * getExecutionEnvironmentInformation
     * (esstar.petalslink.com.data.management.admin
     * ._1.GetExecutionEnvironmentInformation)
     */
    public GetExecutionEnvironmentInformationResponse getExecutionEnvironmentInformation(
            GetExecutionEnvironmentInformation getExecutionEnvironmentInformation)
            throws AdminManagementException {

        GetExecutionEnvironmentInformationResponse result = new GetExecutionEnvironmentInformationResponse();
        EJaxbExecutionEnvironmentInformation info = new EJaxbExecutionEnvironmentInformation();
        result.setExecutionEnvironmentInformation(info);

        // Get local node information
        String nodeName = configurationService.getContainerConfiguration().getName();
        String nodeVersion = "1.0-SNAPSHOT";
        info.setName(nodeName);
        info.setType(EJaxbExecutionEnvironmentInformationTypeType.ESB);
        info.setVersion(nodeVersion);

        easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory factory = new easybox.esstar.petalslink.com.management.model.datatype._1.ObjectFactory();
        EJaxbInterfaceConnectorType itfSubscribeProducer = new EJaxbInterfaceConnectorType();
        itfSubscribeProducer.setId(factory
                .createEJaxbInterfaceConnectorTypeId("resourcesSubscriptionEndpoint"));
        itfSubscribeProducer.setInterfaceName(new QName(
                "http://www.petalslink.com/wsn/service/WsnProducer", "NotificationProducer"));
        itfSubscribeProducer.setEndpointAddress(getProducerEndpoint());
        info.getInterfaceConnector().add(itfSubscribeProducer);

        EJaxbInterfaceConnectorType itfUnSubscribeProducer = new EJaxbInterfaceConnectorType();
        itfUnSubscribeProducer.setId(factory
                .createEJaxbInterfaceConnectorTypeId("resourcesUnSubscriptionEndpoint"));
        itfUnSubscribeProducer
                .setInterfaceName(new QName("http://www.petalslink.com/wsn/service/WsnProducer",
                        "PausableSubscriptionManager"));
        itfUnSubscribeProducer.setEndpointAddress(getProducerEndpoint());
        info.getInterfaceConnector().add(itfUnSubscribeProducer);

        // TODO : add all kernel web services here...

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * getResourceIdentifiers
     * (esstar.petalslink.com.data.management.admin._1.GetResourceIdentifiers)
     */
    public GetResourceIdentifiersResponse getResourceIdentifiers(
            GetResourceIdentifiers getResourceIdentifiers) throws AdminManagementException {
        GetResourceIdentifiersResponse response = new GetResourceIdentifiersResponse();

        try {
            List<ServiceEndpoint> endpoints = getEndpoints();

            for (ServiceEndpoint serviceEndpoint : endpoints) {
                EJaxbResourceIdentifier id = new EJaxbResourceIdentifier();
                id.setId(ResourceIdBuilder.getId(serviceEndpoint));
                id.setResourceType("endpoint");
                response.getResourceIdentifier().add(id);
            }
        } catch (RegistryException e) {
            throw new AdminManagementException(
                    "Error while trying to get endpoints from infrastructure", e);
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * getResourceProperty(javax.xml.namespace.QName)
     */
    public GetResourcePropertyResponse getResourceProperty(QName name)
            throws InvalidResourcePropertyQNameFault, ResourceUnavailableFault,
            ResourceUnknownFault {
        if (name == null) {
            throw new InvalidResourcePropertyQNameFault("Null resource name");
        }
        return NotificationProxy.getResourceProperty(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.admin._1_0.AdminManagement#notify
     * (com.ebmwebsourcing.wsstar.jaxb.notification.base.Notify)
     */
    public void notify(Notify notify) {
        if (log.isDebugEnabled()) {
            log.debug("Admin management notify");
        }
        // TODO: Do it async
        NotificationProxy.notify(notify);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.admin._1_0.AdminManagement#stop
     * ()
     */
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stop");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.admin._1_0.AdminManagement#subscribe
     * (com.ebmwebsourcing.wsstar.jaxb.notification.base.Subscribe)
     */
    public SubscribeResponse subscribe(Subscribe subscribe)
            throws UnacceptableInitialTerminationTimeFault, TopicExpressionDialectUnknownFault,
            InvalidTopicExpressionFault, NotifyMessageNotSupportedFault, TopicNotSupportedFault,
            UnsupportedPolicyRequestFault, ResourceUnknownFault, InvalidFilterFault,
            InvalidProducerPropertiesExpressionFault, UnrecognizedPolicyRequestFault,
            InvalidMessageContentExpressionFault, SubscribeCreationFailedFault {
        if (log.isDebugEnabled()) {
            log.debug("Admin management subscribe");
        }
        if (subscribe == null) {
            throw new InvalidMessageContentExpressionFault("Empty message");
        }
        return NotificationProxy.subscribe(subscribe);
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * unconnectToGovernance(java.lang.String)
     */
    public void unconnectToGovernance(String url) throws AdminManagementException {
        throw new AdminManagementException("unconnect to governance Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.admin._1_0.AdminManagement#
     * unsubscribe(com.ebmwebsourcing.wsstar.jaxb.notification.base.Unsubscribe)
     */
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribe)
            throws UnableToDestroySubscriptionFault, ResourceUnknownFault {
        return NotificationProxy.unsubscribe(unsubscribe);
    }

    /**
     * @param id
     * @return
     */
    private ServiceEndpoint getEndpoint(String id) {
        ServiceEndpoint result = null;
        List<org.ow2.petals.jbi.messaging.endpoint.ServiceEndpoint> endpoints;
        try {
            endpoints = endpointRegistry.getEndpoints();
        } catch (RegistryException e) {
            e.printStackTrace();
            return null;
        }

        for (org.ow2.petals.jbi.messaging.endpoint.ServiceEndpoint serviceEndpoint : endpoints) {
            if (serviceEndpoint.getEndpointName().equals(ResourceIdBuilder.getEndpointName(id))
                    && serviceEndpoint.getLocation().getComponentName()
                            .equals(ResourceIdBuilder.getComponent(id))
                    && serviceEndpoint.getLocation().getContainerName()
                            .equals(ResourceIdBuilder.getContainer(id))
                    && serviceEndpoint.getLocation().getSubdomainName()
                            .equals(ResourceIdBuilder.getDomain(id))) {
                return Adapter.createDSBServiceEndpoint(serviceEndpoint);
            }
        }
        return result;
    }

    /**
     * @return
     */
    private String getProducerEndpoint() {
        return dsbConfigurationService.getWSKernelBaseURL() + "NotificationProducer";
    }

    /**
     * @return
     */
    private String getNotificationEndpoint() {
        return dsbConfigurationService.getWSKernelBaseURL() + "NotificationConsumer";
    }

    /**
     * @return
     * @throws RegistryException
     */
    private List<ServiceEndpoint> getEndpoints() throws RegistryException {
        final List<ServiceEndpoint> result = new ArrayList<ServiceEndpoint>();
        List<org.ow2.petals.jbi.messaging.endpoint.ServiceEndpoint> endpoints = this.endpointRegistry
                .getEndpoints();

        for (org.ow2.petals.jbi.messaging.endpoint.ServiceEndpoint serviceEndpoint : endpoints) {
            ServiceEndpoint se = Adapter.createDSBServiceEndpoint(serviceEndpoint);
            result.add(se);
        }

        return result;
    }

    /**
     * @param description
     * @return
     */
    private Document getDescription(String description) {
        Description desc;
        try {
            desc = WSDL4ComplexWsdlFactory.newInstance().newWSDLReader()
                    .read(XMLHelper.createDocumentFromString(description));
            return WSDL4ComplexWsdlFactory.newInstance().newWSDLWriter().getDocument(desc);

        } catch (WSDL4ComplexWsdlException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private Document getImport(Document doc, String relativePath) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Try to get import from %s", relativePath));
        }

        try {
            Description desc = WSDL4ComplexWsdlFactory.newInstance().newWSDLReader().read(doc);
            Map<URI, org.w3c.dom.Document> imports = desc.deleteImportedDocumentsInWsdl();
            if (imports != null) {
                for (URI uri : imports.keySet()) {
                    System.out.printf("Import URI %s", uri.toString());
                    if (uri.toString().equals(relativePath)) {
                        return imports.get(uri);
                    }
                }
            }
        } catch (WSDL4ComplexWsdlException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
