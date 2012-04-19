/**
 * 
 */
package org.petalslink.dsb.easierbsm.wsnconnector;

import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.petalslink.dsb.monitoring.api.MonitoringAdminClient;
import org.petalslink.dsb.monitoring.api.MonitoringClient;
import org.petalslink.dsb.monitoring.api.MonitoringClientFactory;

/**
 * A factory for easierBSM clients
 * 
 * @author chamerling
 * 
 */
public class EasierBSMClientFactory implements MonitoringClientFactory {

    private static Logger logger = Logger.getLogger(EasierBSMClientFactory.class.getName());

    private MonitoringClient monitoringClient;

    private MonitoringAdminClient monitoringAdminClient;

    private Properties props;
    
    private QName bsmResourceCreationTopic;
    
    private QName bsmRawreportTopic;

    /**
     * 
     */
    public EasierBSMClientFactory(Properties props) {
        this.props = props;
        
        // get the resource creation topic and the raw report one...
        // TODO, for now there are constants for that
        bsmRawreportTopic = EasierBSMConstants.RAWREPORT_TOPIC;
        bsmResourceCreationTopic = EasierBSMConstants.CREATION_TOPIC;
        
        this.monitoringAdminClient = new EasierBSMAdminClient(bsmResourceCreationTopic);
        this.monitoringClient = new EasierBSMClient(bsmRawreportTopic);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petalslink.dsb.monitoring.api.MonitoringClientFactory#getMonitoringClient
     * (java.lang.String)
     */
    public MonitoringClient getMonitoringClient(String endpointName) {
        System.out.println("################# GET BSM MONITORING CLIENT ###################");
        return this.monitoringClient;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.monitoring.api.MonitoringClientFactory#
     * getMonitoringAdminClient(java.lang.String)
     */
    public MonitoringAdminClient getMonitoringAdminClient() {
        System.out.println("################# GET BSM ADMIN MONITORING CLIENT ###################");
        return this.monitoringAdminClient;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petalslink.dsb.monitoring.api.MonitoringClientFactory#
     * getRawMonitoringClient()
     */
    public MonitoringClient getRawMonitoringClient() {
        return this.monitoringClient;
    }
}
