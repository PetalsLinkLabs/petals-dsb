/**
 *
 * Copyright (c) 2012, PetalsLink
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
 *
 */
package org.petalslink.dsb.jbi.se.wsn.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.petalslink.dsb.jbi.se.wsn.NotificationEngine;
import org.petalslink.dsb.jbi.se.wsn.WSNTest;
import org.petalslink.dsb.jbi.se.wsn.api.Topic;

/**
 * @author chamerling
 *
 */
public class ManagementServiceImplTest extends TestCase {
	
	private static Logger logger = Logger.getLogger(ManagementServiceImpl.class.getName());

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	protected final ManagementServiceImpl getManagementServiceImpl() {
		final NotificationEngine engine = new NotificationEngine(logger,
				QName.valueOf("{http://petals.ow2.org}/Service"),
				QName.valueOf("{http://petals.ow2.org}/Interface"), "Endpoint",
				new org.petalslink.dsb.service.client.saaj.Client(), null);

		URL topicSet = WSNTest.class.getResource("/topicset.xml");
		URL tns = WSNTest.class.getResource("/tns.xml");
		engine.init(topicSet, tns);
		
		return new ManagementServiceImpl(engine, logger);
	}
	
	public void testGetTopics() throws Exception {
		ManagementServiceImpl service = getManagementServiceImpl();
		
		List<Topic> topics = service.getTopics();
		assertNotNull(topics);
		// bug in engine where the number of topics is intial + 1 at start...
		assertTrue(topics.size() == 5);
	}
	
	public void testAddTopic() throws Exception {
		ManagementServiceImpl service = getManagementServiceImpl();
		
		Topic topic = new Topic();
		topic.name = "AddMeTest";
		topic.ns = "http://petals.ow2.org/topic/test";
		topic.prefix = "test";
		
		List<Topic> topics = service.getTopics();
		assertFalse(topics.contains(topic));
		
		service.add(topic);
		topics = service.getTopics();
		assertTrue(topics.contains(topic));
		assertTrue(topics.size() > 1);
	}
	
	/*
	public void testRemoveTopic() throws Exception {
		ManagementServiceImpl service = getManagementServiceImpl();
		
		Topic topic = new Topic();
		topic.name = "RemoveMeFoo";
		topic.ns = "http://www.petalslink.org/integration/test/1.0";
		topic.prefix = "itg";
		
		List<Topic> topics = service.getTopics();
		assertTrue(topics.contains(topic));
		
		service.delete(topic);
		topics = service.getTopics();
		assertFalse(topics.contains(topic));
	}
	*/
	
	public void testSetTopics() throws Exception {
	ManagementServiceImpl service = getManagementServiceImpl();
		
		Topic topic = new Topic();
		topic.name = "SetMeTest1";
		topic.ns = "http://petals.ow2.org/topic/test";
		topic.prefix = "test";

		Topic topic2 = new Topic();
		topic2.name = "SetMeTes2";
		topic2.ns = "http://petals.ow2.org/topic/test";
		topic2.prefix = "test";
		
		List<Topic> topics = new ArrayList<Topic>();
		topics.add(topic);
		topics.add(topic2);
		
		service.setTopics(topics);
		List<Topic> engineTopics = service.getTopics();
		
		assertTrue(engineTopics.contains(topic));
		assertTrue(engineTopics.contains(topic2));
		assertTrue(engineTopics.size() == 2);
	}

}
