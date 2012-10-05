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

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.petalslink.dsb.jbi.se.wsn.NotificationEngine;
import org.petalslink.dsb.jbi.se.wsn.api.SubscriptionService;
import org.petalslink.dsb.jbi.se.wsn.api.Topic;
import org.petalslink.dsb.jbi.se.wsn.api.WSNException;

/**
 * @author chamerling
 * 
 */
public class SubscriptionServiceImpl implements SubscriptionService {

	private NotificationEngine engine;

	private Logger logger;

	public SubscriptionServiceImpl(NotificationEngine engine, Logger logger) {
		this.engine = engine;
		this.logger = logger;

	}

	public String subscribe(Topic topic, String subscriber) throws WSNException {
		if (topic == null || subscriber == null) {
			logger.warning("Topic or subscriber is null...");
			throw new WSNException("Topic or subscribe is null");
		}

		return this.engine.subscribe(new QName(topic.ns, topic.name, topic.prefix), subscriber);
	}

	public boolean unsubscribe(String uuid) throws WSNException {
		throw new WSNException("Not implemented");
	}

}
