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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.petalslink.dsb.jbi.se.wsn.NotificationEngine;
import org.petalslink.dsb.jbi.se.wsn.api.NotificationService;
import org.petalslink.dsb.jbi.se.wsn.api.Topic;
import org.petalslink.dsb.jbi.se.wsn.api.WSNException;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;

/**
 * @author chamerling
 * 
 */
public class NotificationServiceImpl implements NotificationService {
	
	private NotificationEngine engine;
	
	private Logger logger;

	/**
	 * 
	 */
	public NotificationServiceImpl(NotificationEngine engine, Logger logger) {
		this.engine = engine;
		this.logger = logger;
	}

	public void notify(Topic topic, String message) throws WSNException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Notify on topic " + topic);
		}
		
		if (topic == null || message == null) {
			throw new WSNException("Null topic or message is not allowed");
		}
		
		QName t = new QName(topic.ns, topic.name, topic.prefix);
		
		// try to create a XML document...
		Document document = null;
		try {
			document = XMLHelper.createDocumentFromString(message);
		} catch (Exception e) {
			final String error = "Can not create a XML document from input message";
			logger.warning(error);
			throw new WSNException(error);
		}
		
		// Do it asynchronously...
		this.engine.notify(t, document);
	}
}
