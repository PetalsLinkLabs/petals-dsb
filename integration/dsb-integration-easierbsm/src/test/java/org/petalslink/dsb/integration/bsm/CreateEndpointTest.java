/**
 * 
 */
package org.petalslink.dsb.integration.bsm;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.integration.bsm.mocks.BSMIntegrationService;
import org.petalslink.dsb.ws.api.ExposerService;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.ServiceEndpoint;

import com.ebmwebsourcing.easierbsm.datacollector.ESBDataCollectorFactoryImpl;
import com.ebmwebsourcing.easierbsm.sla.manager.ESBSLAManagerFactoryImpl;
import com.ebmwebsourcing.easierbsm.sla.manager.api.SLAManagerComponent;
import com.ebmwebsourcing.easierbsm.sla.manager.api.SLAManagerComponentBehaviour;
import com.ebmwebsourcing.easierbsm.wsdm.monitoring.core.ESBWSDMFactoryImpl;
import com.ebmwebsourcing.easierbsm.wsdm.monitoring.core.api.WSDMMonitoringEngine;
import com.ebmwebsourcing.easierbsm.wsdm.monitoring.core.api.WSDMMonitoringEngineBehaviour;
import com.ebmwebsourcing.easyesb.esb.api.ESBFactory;
import com.ebmwebsourcing.easyesb.external.protocol.soap.impl.server.SoapServer;
import com.ebmwebsourcing.easyesb.external.protocol.soap.impl.server.SoapServerConfig;
import com.ebmwebsourcing.easyesb.soa.api.ESBException;
import com.ebmwebsourcing.easyesb.soa.api.node.Node;
import com.ebmwebsourcing.easyesb.soa.api.node.NodeBehaviour;
import com.ebmwebsourcing.easyesb.soa.impl.config.ConfigurationImpl;
import com.ebmwebsourcing.easyesb.technical.service.admin.impl.BSMAdminExtensionFactory;

import easierbsm.petalslink.com.service.wsdmmanager._1_0.AdminExceptionMsg;

import junit.framework.TestCase;

/**
 * @author chamerling
 * 
 */
public class CreateEndpointTest extends TestCase {
    
    static String dsbURL = "http://localhost:7600/petals/ws/";

    /**
     * Creates an endpoint on the DSB and check if it is well created on the BSM
     * node
     * 
     * @throws Exception
     */
    public void testCreateEndpointOnDSBAndCheckBSM() throws Exception {
        
        String businessServiceURL = "http://localhost:5546/integration/service/bsm/BSMIntegrationService";

        /*
        Node node = this.createMonitoringNode(new QName("http://petals.ow2.org",
                "MonitoringBus"), false, "localhost", 9101, 8180);
        
        ESBDataCollectorFactoryImpl.createDataCollector(node, this, conf);
        ESBWSDMFactoryImpl.createWSDMMonitoring(node, this, conf);
        ESBSLAManagerFactoryImpl.createSLAManager(node, this);

        try {
            // connect wsdm manager to data collector
            WSDMMonitoringEngine wsdmMonitoringEngine = (WSDMMonitoringEngine) ((NodeBehaviour)node.findBehaviour(NodeBehaviour.class)).getComponent(ESBWSDMFactoryImpl.WSDM_MONITORING_COMPONENT_NAME);
            ((WSDMMonitoringEngineBehaviour)wsdmMonitoringEngine.findBehaviour(WSDMMonitoringEngineBehaviour.class)).connectToDataCollector();

            // connect sla manager to data collector
            SLAManagerComponent slaManagerComponent = (SLAManagerComponent) ((NodeBehaviour)node.findBehaviour(NodeBehaviour.class)).getComponent(new QName(ESBSLAManagerFactoryImpl.SLA_NAMESPACE, ESBSLAManagerFactoryImpl.SLA_MANAGER_COMPONENT_NAME));
            ((SLAManagerComponentBehaviour)slaManagerComponent.findBehaviour(SLAManagerComponentBehaviour.class)).connectToDataCollector();


            BSMAdminExtensionFactory bsmAdmin = new BSMAdminExtensionFactory();
            bsmAdmin.addSpecificElements(name, node, conf);
            bsmAdmin.exposeBSMAdminService(node);
        } catch (AdminExceptionMsg e) {
            throw new ESBException(e);
        } catch (easierbsm.petalslink.com.service.slamanager._1_0.AdminExceptionMsg e) {
            throw new ESBException(e);
        }


        Service service = null;

        try {
            monitoringBus.start();
            Thread.sleep(100000);
            
            System.out.println("Creating business service");

            // create the business service
            service = CXFHelper.getServiceFromFinalURL(
                    businessServiceURL,
                    BSMIntegrationService.class, new BSMIntegrationService() {

                        public String sayHello() {
                            System.out.println("Business service called");
                            return "Hello";
                        }
                    });

            service.start();

            // bind the service to the bus. This will create a new monitoring
            // endpoint on the monitoring bus...
            
            // check that the DSB is running...
            // TODO
            
            SOAPServiceBinder binder = CXFHelper.getClient(dsbURL, SOAPServiceBinder.class);
            List<ServiceEndpoint> endpoints = binder.bindWebService(businessServiceURL + "?wsdl");
            
            for (ServiceEndpoint endpoint : endpoints) {
                System.out.printf("Expose endpoint %s", endpoint);
                System.out.println();
                ExposerService exposer = CXFHelper.getClient(dsbURL, ExposerService.class);
                exposer.expose();
            }

            System.out.println("Wait that the endpoint has been exposed...");
            // TODO : polling will be better...
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }
            
            // check that we have a monitoring endpoint in BSM

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            monitoringBus.stop();
            service.stop();
        }
        
        */

    }

    private Node createMonitoringNode(QName name, boolean explorer, String host, int port,
            int soap_port) throws ESBException {
        ESBFactory factory = new ESBWSDMFactoryImpl();
        SoapServer soapServer = new SoapServer(new SoapServerConfig(soap_port));
        soapServer.getNioConnector().setMaxIdleTime(3000);
        Node node = factory.createNode(name,
                new ConfigurationImpl(explorer, host, port, soapServer));
        return node;
    }

}
