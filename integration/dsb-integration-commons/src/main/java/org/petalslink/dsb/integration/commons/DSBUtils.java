/**
 * 
 */
package org.petalslink.dsb.integration.commons;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ow2.petals.kernel.ws.api.PEtALSWebServiceException;
import org.ow2.petals.kernel.ws.api.RuntimeService;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.ws.api.StatusService;

/**
 * @author chamerling
 * 
 */
public class DSBUtils {

    /**
     * Waits that the DSB is started for a defined period
     * 
     * @param baseURL
     * @param timeout
     * @param timeUnit
     */
    public static final boolean waitStart(String baseURL, long timeout, TimeUnit timeUnit) {
        String url = getBaseURL(baseURL);

        final StatusService client = CXFHelper.getClient(url, StatusService.class);

        final CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
            public void run() {
                boolean started = false;
                while (!started) {

                    System.out.println("Calling the DSB status service...");
                    try {
                        started = client.isStarted();
                    } catch (RuntimeException e1) {
                        System.err.println("...");
                    }

                    if (!started) {
                        try {
                            System.out
                                    .println("Waiting before calling the DSB state service again...");
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        System.out.println("DSB is started");
                        latch.countDown();
                    }
                }
            }
        });
        t.start();

        try {
            boolean out = latch.await(timeout, timeUnit);
            if (out) {
                System.out.println("Started");
                return true;
            } else {
                System.out.println("Not Started");
                return false;
            }
        } catch (InterruptedException e) {
        }
        return false;
    }

    /**
     * Stops the container...
     * 
     * @param baseURL
     * @return
     */
    public static final void stopContainer(String baseURL) {
        String url = getBaseURL(baseURL);

        RuntimeService client = CXFHelper.getClient(url, RuntimeService.class);
        try {
            client.shutdownContainer();
        } catch (PEtALSWebServiceException e) {
            e.printStackTrace();
        }
    }

    private static final String getBaseURL(String baseURL) {
        return baseURL == null ? Constants.BASE_URL : baseURL;
    }
}
