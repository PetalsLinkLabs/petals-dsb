/**
 * 
 */
package org.petalslink.dsb.easierbsm.wsnconnector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.objectweb.fractal.fraclet.annotation.annotations.FractalComponent;
import org.objectweb.fractal.fraclet.annotation.annotations.Interface;
import org.objectweb.fractal.fraclet.annotation.annotations.LifeCycle;
import org.objectweb.fractal.fraclet.annotation.annotations.Monolog;
import org.objectweb.fractal.fraclet.annotation.annotations.Provides;
import org.objectweb.fractal.fraclet.annotation.annotations.type.LifeCycleType;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.petals.util.oldies.LoggingUtil;
import org.petalslink.dsb.monitoring.api.MonitoringAdminClient;
import org.petalslink.dsb.monitoring.api.MonitoringClient;
import org.petalslink.dsb.monitoring.api.MonitoringClientFactory;

/**
 * A WSN based easierBSM connection factory.
 * 
 * @author chamerling
 * 
 */
@FractalComponent
@Provides(interfaces = { @Interface(name = "service", signature = MonitoringClientFactory.class) })
public class EasierBSMClientFactoryWSNBased implements MonitoringClientFactory {

    @Monolog(name = "logger")
    private Logger logger;

    private LoggingUtil log;

    private org.petalslink.dsb.easierbsm.wsnconnector.EasierBSMClientFactory delegate;

    @LifeCycle(on = LifeCycleType.START)
    protected void start() {
        this.log = new LoggingUtil(this.logger);
        Properties props = new Properties();
        InputStream is = EasierBSMClientFactory.class.getResourceAsStream("/easierbsm.cfg");

        if (is != null) {
            try {
                props.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.warning("Can not configure easierBSM from a null configuration file...");
        }

        if (this.delegate == null) {
            this.delegate = new org.petalslink.dsb.easierbsm.wsnconnector.EasierBSMClientFactory(
                    props);
        }
        this.log.start();
    }

    @LifeCycle(on = LifeCycleType.STOP)
    protected void stop() {
        this.log.start();
        this.delegate = null;
    }

    public MonitoringClient getMonitoringClient(String endpointName) {
        return this.delegate.getMonitoringClient(endpointName);
    }

    public MonitoringClient getRawMonitoringClient() {
        return this.delegate.getMonitoringClient(null);
    }

    public MonitoringAdminClient getMonitoringAdminClient() {
        return this.delegate.getMonitoringAdminClient();
    }

}
