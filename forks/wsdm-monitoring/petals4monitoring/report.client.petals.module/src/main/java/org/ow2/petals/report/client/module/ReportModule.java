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
 */
package org.ow2.petals.report.client.module;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.objectweb.fractal.fraclet.annotation.annotations.FractalComponent;
import org.objectweb.fractal.fraclet.annotation.annotations.Interface;
import org.objectweb.fractal.fraclet.annotation.annotations.LifeCycle;
import org.objectweb.fractal.fraclet.annotation.annotations.Monolog;
import org.objectweb.fractal.fraclet.annotation.annotations.Provides;
import org.objectweb.fractal.fraclet.annotation.annotations.Requires;
import org.objectweb.fractal.fraclet.annotation.annotations.type.LifeCycleType;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.easywsdl.schema.api.XmlException;
import org.ow2.easywsdl.schema.util.SourceHelper;
import org.ow2.petals.jbi.component.context.ComponentContext;
import org.ow2.petals.jbi.messaging.exchange.MessageExchange;
import org.ow2.petals.jbi.messaging.routing.RoutingException;
import org.ow2.petals.jbi.messaging.routing.module.ReceiverModule;
import org.ow2.petals.jbi.messaging.routing.module.SenderModule;
import org.ow2.petals.kernel.configuration.ConfigurationService;
import org.ow2.petals.monitoring.model.rawreport.api.RawReportException;
import org.ow2.petals.monitoring.model.rawreport.api.Report;
import org.ow2.petals.monitoring.model.rawreport.api.ReportList;
import org.ow2.petals.monitoring.model.rawreport.impl.RawReportFactory;
import org.ow2.petals.timestamp.module.TimeStamperHandler;
import org.ow2.petals.transport.util.TransportSendContext;
import org.ow2.petals.util.LoggingUtil;
import org.xml.sax.InputSource;

/**
 * @author rnaudin
 * 
 */
@FractalComponent
@Provides(interfaces = {
		@Interface(name = "reportSender", signature = SenderModule.class),
		@Interface(name = "reportReceiver", signature = ReceiverModule.class) })
public class ReportModule implements SenderModule, ReceiverModule {

	private static final String DEFAULT_BASE_URL = "http://localhost:8085/services/";

	private static final String MONITORING_BASE_URL_KEY = "user.monitoring.base";

	/**
	 * Logger wrapper.
	 */
	private LoggingUtil log;

	private static final String RAW_REPORT_SERVICE_ENDPOINT = "rawReportEndpointClientProxyEndpoint";

	/**
	 * The logger.
	 */
	@Monolog(name = "logger")
	private Logger logger;

	@Requires(name = "configuration", signature = ConfigurationService.class)
	private ConfigurationService configuration;

	private String monitoringBaseURL;

	// map:
	// key = uuid exchange
	// value = length of in message
	private final Map<String, Integer> map = Collections
			.synchronizedMap(new HashMap<String, Integer>());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.petals.jbi.messaging.routing.module.SenderModule#electDestinations
	 * (java.util.Map,
	 * org.ow2.petals.jbi.component.context.ComponentContextImpl,
	 * org.ow2.petals.jbi.messaging.exchange.MessageExchangeImpl)
	 */
	public void electEndpoints(
			Map<org.ow2.petals.jbi.messaging.endpoint.ServiceEndpoint, TransportSendContext> electedDestinations,
			ComponentContext sourceComponentContext, MessageExchange exchange)
			throws RoutingException {
		this.log.call();

		try {

			ReportList reports = this.createReportListFromExchange(exchange);
			this.log.info("In Report Module: " + "electDestinations");
			if (reports.getReports().size() > 0) {
				this.sendReport(reports, this.monitoringBaseURL
						+ RAW_REPORT_SERVICE_ENDPOINT);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RoutingException(e);
		}
	}

	public boolean receiveExchange(MessageExchange exchange,
			ComponentContext arg1) throws RoutingException {
		if (exchange != null) {
			try {
				this.log.info("In Report Module: " + "receiveExchange");
				ReportList reports = this
						.createReportListFromExchange(exchange);
				if (reports.getReports().size() > 0) {
					this.sendReport(reports, this.monitoringBaseURL
							+ exchange.getEndpoint().getEndpointName()
							+ "_WSDMMonitoringClientProxyEndpoint");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RoutingException(e);
			}
		} else {
			// TODO: generate report for timeout error
			System.out.println("ERROR: TIMEOUT");
			throw new RoutingException("Impossible to create report");
		}
		return true;
	}

	private void sendReport(ReportList reports, String address)
			throws Exception {
		ThreadReport reportThread = new ThreadReport();
		reportThread.setLog(this.log);
		reportThread.setAddress(address);
		reportThread.setReportList(reports);
		reportThread.start();
		// pool.returnObject(reportThread);
	}

	private ReportList createReportListFromExchange(MessageExchange exchange)
			throws RawReportException {
		ReportList res = RawReportFactory.getInstance().newReportList();
		try {
			// System.out.println("consumer? = " +
			// MessageExchange.Role.CONSUMER.equals(exchange.getRole()));
			// System.out.println("exchange.getPattern() = " +
			// exchange.getPattern());
			// System.out.println("exchange.getMessage(\"out\") = " +
			// exchange.getMessage("out"));
			// System.out.println("exchange.getFault() = " +
			// exchange.getFault());
			// System.out.println("exchange.getError() = " +
			// exchange.getError());
			// System.out.println("exchange.isTerminated() = " +
			// exchange.isTerminated());

			// flash on request in
			if (MessageExchange.Role.CONSUMER.equals(exchange.getRole())) {

				// handle in request
				if ((MessageExchange.IN_ONLY_PATTERN.equals(exchange
						.getPattern()) && !exchange.isTerminated())
						|| (((MessageExchange.IN_OUT_PATTERN.equals(exchange
								.getPattern()) || (MessageExchange.IN_OPTIONAL_OUT_PATTERN
								.equals(exchange.getPattern())))
								&& (exchange.getMessage("out") == null) && !exchange
								.isTerminated()))) {
					// stock message
					if (!this.map.containsKey(exchange.getExchangeId())) {
						if (exchange.getMessage("in").getContent() instanceof StreamSource) {
							this.map.put(exchange.getExchangeId(),
									((StreamSource) exchange.getMessage("in")
											.getContent()).getInputStream()
											.available());
						} else if (exchange.getMessage("in").getContent() instanceof DOMSource) {
							InputSource source = SourceHelper
									.convertDOMSource2InputSource((DOMSource) exchange
											.getMessage("in").getContent());
							this.map.put(exchange.getExchangeId(), source
									.getByteStream().available());
						} else {
							throw new RawReportException("Source unknown: "
									+ exchange.getMessage("in").getContent()
											.getClass());
						}
					}
				}

				// handle done request
				if (MessageExchange.IN_ONLY_PATTERN.equals(exchange
						.getPattern())
						&& exchange.isTerminated()) {

					// create date client in
					Report report1 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report1);
					report1.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateClientIn());
					report1.setContentLength(this.map.remove(exchange
							.getExchangeId()));

					res.addReport(report1);

					// create date provider in
					Report report2 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report2);
					report2.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateProviderIn());
					if (exchange.getMessage("in") != null) {
						if ((exchange.getMessage("in").getContent() instanceof StreamSource)
								&& ((StreamSource) exchange.getMessage("in")
										.getContent() != null)
								&& (((StreamSource) exchange.getMessage("in")
										.getContent()).getInputStream() != null)) {
							report2.setContentLength(((StreamSource) exchange
									.getMessage("in").getContent())
									.getInputStream().available());
						} else if ((exchange.getMessage("in").getContent() instanceof DOMSource)
								&& ((DOMSource) exchange.getMessage("in")
										.getContent() != null)) {
							InputSource source = SourceHelper
									.convertDOMSource2InputSource((DOMSource) exchange
											.getMessage("in").getContent());
							report2.setContentLength(source.getByteStream()
									.available());
						} else {
							throw new RawReportException("Source unknown");
						}
					} else {
						report2.setContentLength(-1);
					}
					res.addReport(report2);

					// create date provider out
					Report report3 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report3);
					report3.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateProviderOut());
					report3.setContentLength(0);
					report3.setDoesThisRequestInIsAnException(false);
					res.addReport(report3);

					// create date client out
					Report report4 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report4);
					report4.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateClientOut());
					report4.setContentLength(0);
					report4.setDoesThisRequestInIsAnException(false);
					res.addReport(report4);
				}

				// handle out request
				if ((MessageExchange.IN_OUT_PATTERN.equals(exchange
						.getPattern()) || MessageExchange.IN_OPTIONAL_OUT_PATTERN
						.equals(exchange.getPattern()))
						&& ((exchange.getMessage("out") != null)
								|| (exchange.getFault() != null) || (exchange
								.getError() != null))) {

					// create date client in
					Report report1 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report1);
					report1.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateClientIn());
					report1.setContentLength(this.map.remove(exchange
							.getExchangeId()));
					res.addReport(report1);

					// create date provider in
					Report report2 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report2);
					report2.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateProviderIn());
					report2.setContentLength(report1.getContentLength());
					res.addReport(report2);

					// create date provider out
					Report report3 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report3);
					report3.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateProviderOut());

					if (exchange.getMessage("out") != null) {
						if ((exchange.getMessage("out").getContent() instanceof StreamSource)
								&& ((StreamSource) exchange.getMessage("out")
										.getContent() != null)
								&& (((StreamSource) exchange.getMessage("out")
										.getContent()).getInputStream() != null)) {
							report3.setContentLength(((StreamSource) exchange
									.getMessage("out").getContent())
									.getInputStream().available());
						} else if ((exchange.getMessage("out").getContent() instanceof DOMSource)
								&& ((DOMSource) exchange.getMessage("out")
										.getContent() != null)) {
							InputSource source = SourceHelper
									.convertDOMSource2InputSource((DOMSource) exchange
											.getMessage("out").getContent());
							report3.setContentLength(source.getByteStream()
									.available());
						} else {
							throw new RawReportException("Source unknown: "
									+ exchange.getMessage("out").getContent());
						}
					} else {
						report3.setContentLength(0);
					}

					if ((exchange.getFault() != null)
							|| (exchange.getError() != null)) {
						report3.setDoesThisRequestInIsAnException(true);
					} else {
						report3.setDoesThisRequestInIsAnException(false);
					}
					res.addReport(report3);

					// create date client out
					Report report4 = RawReportFactory.getInstance().newReport();
					this.setSOACommonInformation(exchange, report4);
					report4.setDateInGMT(TimeStamperHandler.getInstance()
							.getTimeStamp(exchange).getDateProviderOut());
					report4.setContentLength(report3.getContentLength());
					if ((exchange.getFault() != null)
							|| (exchange.getError() != null)) {
						report4.setDoesThisRequestInIsAnException(true);
					} else {
						report4.setDoesThisRequestInIsAnException(false);
					}
					res.addReport(report4);
				}
			}
		} catch (IOException e) {
			throw new RawReportException(e);
		} catch (XmlException e) {
			throw new RawReportException(e);
		}
		return res;
	}

	private void setSOACommonInformation(MessageExchange exchange, Report report) {
		report.setExchangeId(exchange.getExchangeId());
		if (exchange.getService() != null) {
			report.setServiceName(exchange.getService().toString());
		}
		if (exchange.getEndpoint() != null) {
			report.setEndPoint(exchange.getEndpoint().getEndpointName());
		}
		if (exchange.getOperation() != null) {
			report.setOperationName(exchange.getOperation().toString());
		}
		if (exchange.getInterfaceName() != null) {
			report.setInterfaceName(exchange.getInterfaceName().toString());
		}
		if (exchange.getConsumerEndpoint().toString() != null) {
			report.setConsumerName(exchange.getConsumerEndpoint().toString());
		} else {
			report.setConsumerName(exchange.getConsumerEndpoint().toString());
		}
		if (exchange.getService() != null) {
			report.setServiceProviderName(exchange.getService().toString());
		}
	}

	/**
	 * Start the Fractal component
	 */
	@LifeCycle(on = LifeCycleType.START)
	protected void start() {
		this.log = new LoggingUtil(this.logger);
		this.log.call();

		this.initBaseURL();

	}

	/**
	 * 
	 */
	private void initBaseURL() {
		if (this.configuration.getContainerConfiguration()
				.getUserConfiguration() == null) {
			this.monitoringBaseURL = DEFAULT_BASE_URL;
		} else {
			if (this.configuration.getContainerConfiguration()
					.getUserConfiguration().get(MONITORING_BASE_URL_KEY) != null) {
				this.monitoringBaseURL = this.configuration
						.getContainerConfiguration().getUserConfiguration()
						.get(MONITORING_BASE_URL_KEY);
			}
		}

		if (this.monitoringBaseURL == null) {
			this.monitoringBaseURL = DEFAULT_BASE_URL;
		}

		this.monitoringBaseURL = this.monitoringBaseURL.trim();

		if (!this.monitoringBaseURL.endsWith("/")) {
			this.monitoringBaseURL = this.monitoringBaseURL + "/";
		}
	}

	/**
	 * Stop the Fractal component
	 */
	@LifeCycle(on = LifeCycleType.STOP)
	protected void stop() throws RoutingException {
		this.log.call();
	}

}
