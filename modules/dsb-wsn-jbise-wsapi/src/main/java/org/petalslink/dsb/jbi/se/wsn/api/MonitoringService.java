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
package org.petalslink.dsb.jbi.se.wsn.api;

import javax.jws.WebService;

import org.w3c.dom.Document;

/**
 * @author chamerling
 * 
 */
@WebService
public interface MonitoringService {

	/**
	 * Got a in notification message to be forwarded to subscribers
	 * 
	 * @param uuid
	 * @param topic
	 * @throws WSNException
	 */
	void newInNotifyInput(String uuid, Document payload, Topic topic, long timestamp)
			throws WSNException;
	
	/**
	 * The input notification message has been forwarded to subscribers
	 * 
	 * @param uuid
	 * @param topic
	 * @param timestamp
	 * @throws WSNException
	 */
	void newInNotifyOutput(String uuid, Document payload, Topic topic, long timestamp)
			throws WSNException;

	/**
	 * Got error while sending notification to subscriber
	 * 
	 * @param uuid
	 * @param to
	 * @param topic
	 * @param error
	 * @param timestamp
	 * @throws WSNException
	 */
	void newInNotifyError(String uuid, Document payload, String to, Topic topic, long timestamp,
			Exception error) throws WSNException;

	/**
	 * The output notification has been successfully sent to subscriber
	 * 
	 * @param uuid
	 *            used to correlate with {@link #newInNotify(String, Topic)}
	 *            since we can have N out for one in.
	 * @param to
	 * @param topic
	 * @throws WSNException
	 */
	void newOutNotify(String uuid, Document payload, String to, Topic topic, long timestamp)
			throws WSNException;

	/**
	 * Fail to send the notification to the subscriber
	 * 
	 * @param uuid
	 * @param to
	 * @param topic
	 * @param error
	 * @throws WSNException
	 */
	void newOutNotifyError(String uuid, Document payload, String to, Topic topic, long timestamp,
			Exception error) throws WSNException;

}
