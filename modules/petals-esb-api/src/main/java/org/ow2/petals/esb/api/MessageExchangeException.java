/**
 * PETALS: PETALS Services Platform Copyright (C) 2009 EBM WebSourcing
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * 
 * Initial developer(s): EBM WebSourcing
 */
package org.ow2.petals.esb.api;

import javax.xml.ws.WebFault;

/**
 * @author chamerling - eBM WebSourcing
 *
 */
@WebFault
public class MessageExchangeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1468321372938822361L;

    /**
     * 
     */
    public MessageExchangeException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public MessageExchangeException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public MessageExchangeException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public MessageExchangeException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}