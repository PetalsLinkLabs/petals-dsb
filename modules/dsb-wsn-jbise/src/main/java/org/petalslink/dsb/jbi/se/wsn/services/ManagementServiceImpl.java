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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.petalslink.dsb.jbi.se.wsn.NotificationEngine;
import org.petalslink.dsb.jbi.se.wsn.api.ManagementService;
import org.petalslink.dsb.jbi.se.wsn.api.Topic;

/**
 * @author chamerling
 * 
 */
public class ManagementServiceImpl implements ManagementService {

	private NotificationEngine engine;

	private Logger logger;
	
	final Object lock = new Object();

	public ManagementServiceImpl(NotificationEngine engine, Logger logger) {
		this.engine = engine;
		this.logger = logger;
	}

	public void add(Topic topic) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Adding topic " + topic);
		}
		
		List<Topic> topics = getTopics();
		if (topics == null) {
			return;
		}
		if (topics.contains(topic)) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Topic is already in the list " + topic);
			}
			return;
		}
		
		// Bad Hack, remove the RPUpdate topic from the list. This one is
		// malformed!
		Iterator<Topic> iter = topics.iterator();
		while (iter.hasNext()) {
			Topic t = iter.next();
			if ("RPUpdate".equals(t.name)) {
				iter.remove();
			}
		}
		
		topics.add(topic);
		
		synchronized (lock) {
			logger.fine("Updating topics for add");
			this.engine.updateTopicSet(topics);
		}
	}

	public boolean delete(Topic topic) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Deleting topic " + topic);
		}
		
		List<Topic> topics = getTopics();
		if (topics == null) {
			return false;
		}
		// Bad Hack, remove the RPUpdate topic from the list. This one is
		// malformed!
		Iterator<Topic> iter = topics.iterator();
		while (iter.hasNext()) {
			Topic t = iter.next();
			if ("RPUpdate".equals(t.name)) {
				iter.remove();
			}
		}
		
		topics.remove(topic);
		
		synchronized (lock) {
			logger.info("Updating topics for delete");
			this.engine.updateTopicSet(topics);
		}
		return true;
	}

	public List<Topic> getTopics() {
		return this.engine.getTopics();
	}
	
	public void setTopics(List<Topic> topics) {
		if (topics == null || topics.size() == 0) {
			logger.warning("Can not update with null topics...");
			return;
		}
		this.engine.updateTopicSet(topics);
	}
}
