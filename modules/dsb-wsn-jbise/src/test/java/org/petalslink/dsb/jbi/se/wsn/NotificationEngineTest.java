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
package org.petalslink.dsb.jbi.se.wsn;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.petalslink.dsb.jbi.se.wsn.api.Topic;

/**
 * @author chamerling
 * 
 */
public class NotificationEngineTest extends TestCase {
	
    Logger logger = Logger.getLogger(NotificationEngineTest.class.getName());

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

	public void testGetTopics() throws Exception {
		// create the engine with a list of topics and check that they are all
		// here

		final NotificationEngine engine = new NotificationEngine(logger,
				QName.valueOf("{http://petals.ow2.org}/Service"),
				QName.valueOf("{http://petals.ow2.org}/Interface"), "Endpoint",
				new org.petalslink.dsb.service.client.saaj.Client(), null);

		URL topicSet = WSNTest.class.getResource("/topicset.xml");
		URL tns = WSNTest.class.getResource("/tns.xml");
		engine.init(topicSet, tns);
		
		// we should be able to retrieve 4 topics
		List<Topic> topics = engine.getTopics();
		System.out.println(topics);

		// engine adds one for topics changes... Sadly this topic is removed when an update is done
		assertTrue(topics.size() == 5);
		
		Topic t1 = new Topic();
		t1.name = "BusinessIntegrationTopic";
		t1.ns = "http://www.petalslink.org/integration/test/1.0";
		t1.prefix = "itg";
		
		Topic t2 = new Topic();
		t2.name = "RemoveMe";
		t2.ns = "http://www.petalslink.org/integration/test/1.0";
		t2.prefix = "itg";
		
		Topic t3 = new Topic();
		t3.name = "RemoveMeFoo";
		t3.ns = "http://www.petalslink.org/integration/test/1.0";
		t3.prefix = "itg";
		
		Topic t4 = new Topic();
		t4.name = "RemoveMeBar";
		t4.ns = "http://www.petalslink.org/integration/test/1.0";
		t4.prefix = "itg";
		
		assertTrue(topics.contains(t1));
		assertTrue(topics.contains(t2));
		assertTrue(topics.contains(t3));
		assertTrue(topics.contains(t4));
	}
	
	public void testUpdateTopics() throws Exception {
		
		final NotificationEngine engine = new NotificationEngine(logger,
				QName.valueOf("{http://petals.ow2.org}/Service"),
				QName.valueOf("{http://petals.ow2.org}/Interface"), "Endpoint",
				new org.petalslink.dsb.service.client.saaj.Client(), null);

		URL topicSet = WSNTest.class.getResource("/topicset.xml");
		URL tns = WSNTest.class.getResource("/tns.xml");
		engine.init(topicSet, tns);
		
		// we should be able to retrieve 4 topics
		List<Topic> topics = engine.getTopics();
		assertTrue(topics.size() == 5);
		
		List<Topic> list = new ArrayList<Topic>();
		Topic t = new Topic();
		t.name = "TheOnlyOne";
		t.ns = "http://www.petalslink.org/integration/test/1.0";
		t.prefix = "itg";
		list.add(t);
		
		// update removes the topic added by the engine for topic updates...
		// so instead of 2 we have one topic
		engine.updateTopicSet(list);
		
		topics = engine.getTopics();
		
		System.out.println(topics);
		assertEquals(1, topics.size());
		assertTrue(topics.contains(t));
		
	}

}
