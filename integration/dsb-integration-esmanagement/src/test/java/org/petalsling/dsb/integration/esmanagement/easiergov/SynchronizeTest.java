/**
 * 
 */
package org.petalsling.dsb.integration.esmanagement.easiergov;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.petalslink.dsb.integration.commons.Constants;
import org.petalslink.dsb.integration.commons.DSBUtils;
import org.petalslink.dsb.jaxws.JAXWSHelper;

import com.ebmwebsourcing.easiergov.WSContainer;
import com.ebmwebsourcing.easiergov.client.impl.ConnexionManagerClientImpl;
import com.petalslink.easiergov.core.config.Configuration;
import com.petalslink.easiergov.core.container.Container;
import com.petalslink.easiergov.core.container.Server;
import com.petalslink.easiergov.core.impl.config.ConfigurationImpl;
import com.petalslink.esstar.execution_environment_synchronizer_impl._1.ConnectToEnvironment;
import com.petalslink.esstar.execution_environment_synchronizer_impl._1.ConnectToEnvironmentResponse;

/**
 * @author chamerling
 * 
 */
public class SynchronizeTest extends TestCase {

    Container container = null;

    /**
     * @param name
     */
    public SynchronizeTest(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Configuration conf = new ConfigurationImpl();

        try {
            container = new WSContainer(conf);
            container.start();
        } finally {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        System.out.println("Stopping the governance container");
        if (container != null) {
            container.stop();
        }
    }

    public void testSynschronizeDSB() throws Exception {
        if (false) {
            System.out.println("Waiting that the DSB is up...");
            boolean started = DSBUtils.waitStart(Constants.BASE_URL, 10, TimeUnit.SECONDS);
            if (!started) {
                fail("The DSB is not started");
            }
        }

        System.out.println("Synchronize easierGov and the DSB...");

        List<Server> servers = container.getServers();
        Iterator<Server> iter = servers.iterator();
        Server s = null;
        boolean found = false;

        while (iter.hasNext() && !found) {
            s = iter.next();
            if ("Connexion API".equals(s.getName())) {
                found = true;
            }
        }

        ConnexionManagerClientImpl client = new ConnexionManagerClientImpl(s.getAddress());
        ConnectToEnvironment connect = new ConnectToEnvironment();
        // TODO : need a DSB registry where we can retrive service from its java
        // interface remotely
        connect.setEndpointAddress(Constants.BASE_URL + "/AdminManagement");
        ConnectToEnvironmentResponse response = client.connectToEnvironment(connect);
        
    }

}
