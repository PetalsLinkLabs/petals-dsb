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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Raw notification service.
 * 
 * @author chamerling
 * 
 */
@WebService
public interface NotificationService {

	/**
	 * Send a notification to the given topic
	 * 
	 * @param topic
	 * @param message
	 *            the XML message as string...
	 * @throws WSNException
	 */
	@WebMethod
	void notify(@WebParam(name = "topic") Topic topic,
			@WebParam(name = "message") String message) throws WSNException;

}
