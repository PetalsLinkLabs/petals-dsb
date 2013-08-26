/**
 *
 * Copyright (c) 2013, Linagora
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
package org.ow2.petals.cloud.init;

import java.util.Map;
import java.util.Properties;

/**
 * Initialize the node for a cloud environment.
 * This must be called before all other services since it will configure the node (generates files if needed)...
 *
 * @author Christophe Hamerling - chamerling@linagora.com
 */
public interface CloudInit {

    /**
     * Configure the node for a Cloud environment
     */
    void configure() throws CloudInitException;

    /**
     * Get the user properties
     */
    Properties getUserProperties();

    /**
     * Get the cloud platform properties
     */
    Properties getPlatformProperties();

}
