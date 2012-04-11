/**
 * 
 */
package org.petalslink.dsb.integration.commons;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.ws.api.StatusService;

import junit.framework.TestCase;

/**
 * @author chamerling
 * 
 */
public class DSBUtilsTest extends TestCase {

    /**
     * @param name
     */
    public DSBUtilsTest(String name) {
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

    public void testWaitStartNotStarted() throws Exception {
        boolean result = DSBUtils.waitStart("http://localhost:8765/integration/dsb/notstart", 10,
                TimeUnit.SECONDS);
        assertFalse(result);
    }

    public void testWaitStartAlreadyStarted() {
        String url = "http://localhost:8765/integration/dsb/started";
        Service service = CXFHelper.getService(url, StatusService.class, new StatusService() {

            public boolean isStopping() {
                return false;
            }

            public boolean isStarting() {
                return false;
            }

            public boolean isStarted() {
                return true;
            }

            public long getStartedAt() {
                return 0;
            }
        });

        try {
            service.start();

            boolean result = DSBUtils.waitStart(url, 10, TimeUnit.SECONDS);
            assertTrue(result);
        } finally {
            service.stop();
        }
    }

    public void testWaitStartWillStart() throws Exception {
        String url = "http://localhost:8765/integration/dsb/willstart";
        final AtomicBoolean started = new AtomicBoolean(false);
        final AtomicInteger calls = new AtomicInteger(0);

        Service service = CXFHelper.getService(url, StatusService.class, new StatusService() {

            public boolean isStopping() {
                return false;
            }

            public boolean isStarting() {
                return false;
            }

            public boolean isStarted() {
                // first call is false, next one is true...
                System.out.println("Current DSB status is " + started.get());
                calls.incrementAndGet();
                return started.getAndSet(true);
            }

            public long getStartedAt() {
                return 0;
            }
        });

        try {
            service.start();

            boolean result = DSBUtils.waitStart(url, 10, TimeUnit.SECONDS);
            
            assertTrue(result);
            assertEquals(2, calls.get());
            
        } finally {
            service.stop();
        }
    }
}
