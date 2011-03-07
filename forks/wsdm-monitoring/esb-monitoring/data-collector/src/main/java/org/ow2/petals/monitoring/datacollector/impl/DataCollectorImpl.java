/**
 * MonitoringEngine-Core - SOA Tools Platform.
 * Copyright (c) 2008 EBM Websourcing, http://www.ebmwebsourcing.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * -------------------------------------------------------------------------
 * $id.java
 * -------------------------------------------------------------------------
 */
package org.ow2.petals.monitoring.datacollector.impl;

import javax.xml.namespace.QName;

import org.objectweb.fractal.fraclet.extensions.Membrane;
import org.ow2.petals.esb.kernel.api.ESBException;
import org.ow2.petals.esb.kernel.impl.endpoint.context.EndpointInitialContextImpl;
import org.ow2.petals.esb.kernel.impl.entity.ClientAndProviderImpl;
import org.ow2.petals.monitoring.datacollector.api.DataCollector;
import org.ow2.petals.monitoring.datacollector.api.RawReportClientEndpoint;
import org.ow2.petals.monitoring.datacollector.api.RawReportService;

/**
 * @author Nicolas Salatge - eBM WebSourcing
 */
@org.objectweb.fractal.fraclet.annotations.Component
@Membrane(controller = "composite")
public class DataCollectorImpl extends ClientAndProviderImpl implements
		DataCollector {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public RawReportClientEndpoint createRawReportClientEndpoint(
			final QName name) throws ESBException {
		final RawReportClientEndpoint res = this.createClientEndpoint(name,
				"rawReportClientEndpoint", RawReportClientEndpointImpl.class,
				new EndpointInitialContextImpl(5));
		res.setBehaviourClass(RawReportClientEndpointBehaviourImpl.class);
		return res;
	}

	public RawReportService createRawReportService(final QName service)
			throws ESBException {
		return this.createService(service, "rawReportService",
				RawReportServiceImpl.class);
	}

}
