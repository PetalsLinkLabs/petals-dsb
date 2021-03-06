/**
 * PETALS - PETALS Services Platform. Copyright (c) 2007 EBM Websourcing,
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

package org.ow2.petals.binding.soap.listener.incoming.jetty;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A jetty logger implementation based on the root logger of the component.
 * 
 * @author chamerling - eBM WebSourcing
 */
public class JettyLogger implements org.mortbay.log.Logger {

    /**
     * The component logger
     */
    protected Logger logger;
    
    /**
     * Creates a new instance of {@link JettyLogger}
     * 
     * @param name
     * @param logger
     */
    public JettyLogger(final java.util.logging.Logger logger) {
        this.logger = logger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#debug(java.lang.String, java.lang.Object,
     * java.lang.Object)
     */
    public void debug(final String arg0, final Object arg1, final Object arg2) {
        logger.log(Level.FINEST, arg0, new Object[] { arg1, arg2 });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(final String arg0, final Throwable arg1) {
        logger.log(Level.FINEST, arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#getLogger(java.lang.String)
     */
    public org.mortbay.log.Logger getLogger(final String arg0) {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#info(java.lang.String, java.lang.Object,
     * java.lang.Object)
     */
    public void info(final String arg0, final Object arg1, final Object arg2) {
        logger.log(Level.INFO, arg0, new Object[] { arg1, arg2 });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#setDebugEnabled(boolean)
     */
    public void setDebugEnabled(final boolean arg0) {
        logger.setLevel(Level.FINEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#warn(java.lang.String, java.lang.Object,
     * java.lang.Object)
     */
    public void warn(final String arg0, final Object arg1, final Object arg2) {
        logger.log(Level.WARNING, arg0, new Object[] { arg1, arg2 });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mortbay.log.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn(final String arg0, final Throwable arg1) {
        logger.log(Level.WARNING, arg0, arg1);
    }
}
