/**
 * 
 * Copyright (c) 2012, PetalsLink
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 */
package org.petalslink.dsb.jbi.se.wsn;

import java.util.concurrent.atomic.AtomicLong;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 * @author chamerling
 * 
 */
public class StatsTest extends TestCase {

    /**
     * @param name
     */
    public StatsTest(String name) {
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

    public void testGetNotifyPerTopicEmpty() throws Exception {
        AtomicLong calls = Stats.getInstance().getInNotifyPerTopic().get(QName.valueOf("foo"));
        assertNull(calls);
    }

    public void notestGetNotifyPerTopic() throws Exception {
        QName topic = QName.valueOf("bar");
        Stats.getInstance().newInNotifyCall(topic);
        System.out.println(Stats.getInstance().getInNotifyPerTopic().get(topic));
        assertNotNull(Stats.getInstance().getInNotifyPerTopic().get(topic));
        assertEquals(1, Stats.getInstance().getInNotifyPerTopic().get(topic).get());
    }

}
