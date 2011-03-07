/**
 * PETALS - PETALS Services Platform. Copyright (c) 2009 EBM Websourcing,
 * http://www.ebmwebsourcing.com/
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * -------------------------------------------------------------------------
 * $Id$
 * -------------------------------------------------------------------------
 */
package org.ow2.petals.tools.generator.jbi.api;

/**
 * @author Christophe HAMERLING - eBM WebSourcing
 * 
 */
public class JBIGenerationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7634672889744290023L;

	/**
	 * Creates a new instance of ConvertException
	 */
	public JBIGenerationException() {
	}

	/**
	 * Creates a new instance of ConvertException
	 * 
	 * @param message
	 */
	public JBIGenerationException(String message) {
		super(message);
	}

	/**
	 * Creates a new instance of ConvertException
	 * 
	 * @param cause
	 */
	public JBIGenerationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new instance of ConvertException
	 * 
	 * @param message
	 * @param cause
	 */
	public JBIGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

}
