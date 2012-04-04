/**
 * 
 */
package org.petalslink.dsb.kernel.esmanagement;

import java.util.List;

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
import org.ow2.petals.jbi.messaging.registry.EndpointRegistry;
import org.ow2.petals.jbi.messaging.registry.RegistryException;
import org.ow2.petals.util.oldies.LoggingUtil;
import org.petalslink.dsb.jbi.Adapter;
import org.petalslink.dsb.ws.api.DSBWebServiceException;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.SOAPServiceExposer;
import org.petalslink.dsb.ws.api.ServiceEndpoint;

import com.ebmwebsourcing.wsstar.jaxb.notification.base.Notify;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Subscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.SubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Unsubscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.UnsubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.GetResourcePropertyResponse;

import esstar.petalslink.com.data.management.user._1.Bind;
import esstar.petalslink.com.data.management.user._1.BindResponse;
import esstar.petalslink.com.data.management.user._1.Deploy;
import esstar.petalslink.com.data.management.user._1.DeployResponse;
import esstar.petalslink.com.data.management.user._1.Proxify;
import esstar.petalslink.com.data.management.user._1.ProxifyResponse;
import esstar.petalslink.com.service.management.user._1_0.UserManagementException;

/**
 * @author chamerling
 * 
 */
@WebService
@FractalComponent
@Provides(interfaces = { @Interface(name = "service", signature = esstar.petalslink.com.service.management.user._1_0.UserManagement.class) })
public class UserManagement implements
        esstar.petalslink.com.service.management.user._1_0.UserManagement {

    @Monolog(name = "logger")
    private Logger logger;

    private LoggingUtil log;

    @Requires(name = "soapbinder", signature = SOAPServiceBinder.class, contingency = Contingency.OPTIONAL)
    private SOAPServiceBinder binder;

    @Requires(name = "soapexposer", signature = SOAPServiceExposer.class, contingency = Contingency.OPTIONAL)
    private SOAPServiceExposer exposer;

    @Requires(name = "endpoint", signature = EndpointRegistry.class)
    private EndpointRegistry endpointRegistry;

    @LifeCycle(on = LifeCycleType.START)
    protected void start() {
        this.log = new LoggingUtil(this.logger);
    }

    @LifeCycle(on = LifeCycleType.STOP)
    protected void stop() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.user._1_0.UserManagement#bind
     * (esstar.petalslink.com.data.management.user._1.Bind)
     */
    public BindResponse bind(Bind bind) throws UserManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Bind service to the bus, external address %s and WSDL %s",
                    bind.getExternalAddress(), bind.getWsdl().getUrl().getValue());
        }

        if (binder == null) {
            throw new UserManagementException("Can not find any valid binder to bind service");
        }

        BindResponse response = new BindResponse();

        if (bind != null && bind.getWsdl() != null && bind.getWsdl().getUrl() != null
                && bind.getWsdl().getUrl().getValue() != null) {
            try {
                List<ServiceEndpoint> endpoints = binder.bindWebService(bind.getWsdl().getUrl()
                        .getValue());
                if (endpoints != null) {
                    for (ServiceEndpoint serviceEndpoint : endpoints) {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "Service is bound, new endpoint %s, service %s, interface %s",
                                    serviceEndpoint.getEndpoint(), serviceEndpoint.getService(),
                                    serviceEndpoint.getItf());
                        }
                        // the current API does not handle more than one
                        // endpoint per bind but the DSB can do it...
                        response.setEndpointName(serviceEndpoint.getEndpoint());
                    }
                }
            } catch (DSBWebServiceException e) {
                log.warning("Got a problem while binding webservice", e);
                throw new UserManagementException("Got a problem while binding webservice", e);
            }
        } else {
            throw new UserManagementException("Bad parameters");
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.user._1_0.UserManagement#deploy
     * (esstar.petalslink.com.data.management.user._1.Deploy)
     */
    public DeployResponse deploy(Deploy deploy) throws UserManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Deploying resource into the DSB");
        }
        throw new UserManagementException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.user._1_0.UserManagement#expose
     * (javax.xml.namespace.QName, java.lang.String)
     */
    public String expose(QName service, String endpoint) throws UserManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Expose service %s endpoint %s", service, endpoint);
        }

        if (service == null || endpoint == null) {
            throw new UserManagementException(String.format(
                    "Null paramater service=%s, endpoint=%s", service, endpoint));
        }

        if (this.exposer == null) {
            throw new UserManagementException("Can not find any exposer to expose service");
        }

        // retrieve the endpoint from the given references
        try {
            org.ow2.petals.jbi.messaging.endpoint.ServiceEndpoint se = this.endpointRegistry
                    .getEndpoint(service, endpoint);
            if (se != null) {
                ServiceEndpoint expose = new ServiceEndpoint();
                this.exposer.expose(expose);
            } else {
                throw new UserManagementException(String.format(
                        "Can not get any valid endpoint for endpoint name %s and service %s",
                        endpoint, service));
            }
        } catch (RegistryException e) {
            log.warning("Got a problem while exposing service", e);
            throw new UserManagementException("Got a problem while exposing service", e);
        } catch (DSBWebServiceException e) {
            log.warning("Got a problem while exposing service", e);
            throw new UserManagementException("Got a problem while exposing service", e);
        }
        return "http://TODO";
    }

    /*
     * (non-Javadoc)
     * 
     * @see esstar.petalslink.com.service.management.user._1_0.UserManagement#
     * getResourceProperty(javax.xml.namespace.QName)
     */
    public GetResourcePropertyResponse getResourceProperty(QName name)
            throws ResourceUnavailableFault, InvalidResourcePropertyQNameFault,
            ResourceUnknownFault {
        if (log.isDebugEnabled()) {
            log.debug("Get resouce property %s", name);
        }

        return NotificationProxy.getResourceProperty(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.user._1_0.UserManagement#notify
     * (com.ebmwebsourcing.wsstar.jaxb.notification.base.Notify)
     */
    public void notify(Notify notify) {
        if (log.isDebugEnabled()) {
            log.debug("Business notify...");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.user._1_0.UserManagement#proxify
     * (esstar.petalslink.com.data.management.user._1.Proxify)
     */
    public ProxifyResponse proxify(Proxify proxify) throws UserManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Proxify service %s", proxify.getWsdl().getUrl().getValue());
        }

        if (proxify == null) {
            throw new UserManagementException("Not implemented, bind should do the same...");
        }

        if (binder == null) {
            throw new UserManagementException("Can not find any valid binder to bind service");
        }

        if (exposer == null) {
            throw new UserManagementException("Can not find any valid exposer to expose service");
        }

        ProxifyResponse response = null;
        if (proxify != null && proxify.getWsdl() != null && proxify.getWsdl().getUrl() != null
                && proxify.getWsdl().getUrl().getValue() != null) {
            response = new ProxifyResponse();

            List<ServiceEndpoint> endpoints = null;
            try {
                endpoints = binder.bindWebService(proxify.getWsdl().getUrl().getValue());
            } catch (DSBWebServiceException e) {
                log.warning("Got a problem while bindind webservice, proxify fails", e);
                throw new UserManagementException(
                        "Got a problem while proxifying webservice, proxify fails", e);
            }

            if (endpoints != null) {
                for (ServiceEndpoint serviceEndpoint : endpoints) {
                    try {
                        boolean result = exposer.expose(serviceEndpoint);
                    } catch (DSBWebServiceException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Got a problem while exposing webservice, trying next endpoint if any...");
                        } else {
                            log.warning(
                                    "Got a problem while exposing webservice, trying next endpoint if any...",
                                    e);
                        }
                    }
                }
            }

            response.setExternalAddress("http://TODO");

        } else {
            throw new UserManagementException("Null parameter");
        }

        return response;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.user._1_0.UserManagement#subscribe
     * (com.ebmwebsourcing.wsstar.jaxb.notification.base.Subscribe)
     */
    public SubscribeResponse subscribe(Subscribe subscribe) throws NotifyMessageNotSupportedFault,
            InvalidFilterFault, InvalidTopicExpressionFault, TopicExpressionDialectUnknownFault,
            UnsupportedPolicyRequestFault, SubscribeCreationFailedFault,
            UnacceptableInitialTerminationTimeFault, UnrecognizedPolicyRequestFault,
            TopicNotSupportedFault, InvalidProducerPropertiesExpressionFault, ResourceUnknownFault,
            InvalidMessageContentExpressionFault {
        if (log.isDebugEnabled()) {
            log.debug("User management subscribe");
        }
        return NotificationProxy.subscribe(subscribe);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * esstar.petalslink.com.service.management.user._1_0.UserManagement#unsubscribe
     * (com.ebmwebsourcing.wsstar.jaxb.notification.base.Unsubscribe)
     */
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribe)
            throws UnableToDestroySubscriptionFault, ResourceUnknownFault {
        if (log.isDebugEnabled()) {
            log.debug("User management unsubscribe");
        }
        return NotificationProxy.unsubscribe(unsubscribe);
    }

}
