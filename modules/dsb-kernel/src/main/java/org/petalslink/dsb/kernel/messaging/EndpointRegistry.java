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
package org.petalslink.dsb.kernel.messaging;

import java.util.List;

import org.ow2.petals.kernel.api.service.ServiceEndpoint;

/**
 * @author chamerling - eBM WebSourcing
 *
 */
public interface EndpointRegistry {

    /**
     * Store a service which has been bound
     * 
     * @param endpoint
     */
    void storeBoundService(ServiceEndpoint endpoint);

    /**
     * Delete a service which has been bound
     */
    void deleteBoundService();

    /**
     * Get a list of bound services
     * 
     * @return
     */
    List<ServiceEndpoint> getBoundServices();

}
