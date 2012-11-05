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
 */

package org.petalslink.dsb.jbi.se.wsn;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.jbi.JBIException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.ow2.easywsdl.wsdl.api.Endpoint;
import org.ow2.petals.component.framework.ComponentInformation;
import org.ow2.petals.component.framework.PetalsBindingComponent;
import org.ow2.petals.component.framework.api.Wsdl;
import org.ow2.petals.component.framework.util.ServiceEndpointKey;
import org.ow2.petals.component.framework.util.WSDLUtilImpl;
import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.jbi.se.wsn.api.ManagementService;
import org.petalslink.dsb.jbi.se.wsn.api.MonitoringService;
import org.petalslink.dsb.jbi.se.wsn.api.NotificationService;
import org.petalslink.dsb.jbi.se.wsn.api.StatsService;
import org.petalslink.dsb.jbi.se.wsn.api.SubscriptionService;
import org.petalslink.dsb.jbi.se.wsn.services.ManagementServiceImpl;
import org.petalslink.dsb.jbi.se.wsn.services.NotificationServiceImpl;
import org.petalslink.dsb.jbi.se.wsn.services.StatsServiceImpl;
import org.petalslink.dsb.jbi.se.wsn.services.SubscriptionServiceImpl;
import org.petalslink.dsb.notification.commons.PropertiesConfigurationProducer;
import org.petalslink.dsb.notification.commons.api.ConfigurationProducer;
import org.petalslink.dsb.service.client.Client;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

/**
 * The dsb-wsn-jbise Binding Component.
 * 
 * <br>
 * <b>NOTE : </b>This class has to be used only if the component developper
 * wants to customize the main component class. In general, using the
 * org.objectweb.petals.component.framework.bc.DefaultBindingComponent class is
 * enough. If so, change the value in the JBI descriptor file.
 * 
 * @author
 * 
 */
public class Component extends PetalsBindingComponent {

	public static final String FILE_CFG = "notification.cfg";

	public static final String TOPICS_NS_FILE = "topics/business-topicns-rpupdate.xml";

	public static final String TOPICSET_FILE = "topics/business-topicset.xml";

	public static final String ENDPOINT_NAME = "endpoint";

	public static final String INTERFACE_NAME = "interface";

	public static final String SERVICE_NAME = "service";

	protected NotificationEngine engine;

	protected Client httpClient;

	private Map<ServiceEndpointKey, Wsdl> WSNEP;

	protected List<Service> ws;

	protected StatsService stats;

	protected ManagementService managementService;
	
	protected SubscriptionService subscriptionService;
	
	protected NotificationService notificationService;
	
	protected MonitoringService monitoringService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.petals.component.framework.PetalsBindingComponent#postDoInit()
	 */
	@Override
	protected void postDoInit() throws JBIException {

		this.ws = new ArrayList<Service>();
		this.WSNEP = new ConcurrentHashMap<ServiceEndpointKey, Wsdl>();
		this.initializeNotificationEngine();
		this.addServices();
	}
	
	/**
	 * 
	 */
	protected void addServices() {
		this.stats = new StatsServiceImpl(getLogger());
		ws.add(getService(StatsService.class, stats, "StatsService"));

		this.managementService = new ManagementServiceImpl(this.engine,
				getLogger());
		ws.add(getService(ManagementService.class, managementService,
				"ManagementService"));
		
		this.subscriptionService = new SubscriptionServiceImpl(this.engine, getLogger());
		ws.add(getService(SubscriptionService.class, this.subscriptionService, "SubscriptionService"));
		
		this.notificationService = new NotificationServiceImpl(this.engine, getLogger());
		ws.add(getService(NotificationService.class, this.notificationService, "NotificationService"));
	
		doAddServices();
	}
	
	/**
	 * 
	 */
	protected void doAddServices() {
		// To be overrided, check #addServices
	}
	
	protected Service getService(Class<?> clazz, Object bean, String serviceName) {
		// TODO : Push in CDK
		String port = this.getContainerConfiguration("http.port") == null ? "8079"
				: this.getContainerConfiguration("http.port");
		String host = this.getContainerConfiguration("http.host") == null ? "localhost"
				: this.getContainerConfiguration("http.host");
		
		return CXFHelper.getServiceFromFinalURL("http://" + host + ":" + port
				+ "/" + this.getContext().getComponentName() + "/" + serviceName,
				clazz, bean);
		
	}

	/**
	 * Initialize based on local resources
	 * 
	 * @throws JBIException
	 */
	protected void initializeNotificationEngine() throws JBIException {
		// get the configuration files...
		Properties props = new Properties();
		try {
			props.load(Component.class.getClassLoader().getResourceAsStream(
					"notification.cfg"));
		} catch (IOException e) {
			throw new JBIException(
					"Can not find the notification configuration file");
		}

		URL topics = Component.class.getClassLoader()
				.getResource(TOPICSET_FILE);
		if (topics == null) {
			throw new JBIException(
					"Can not find the notification topicnamespace configuration file");
		}

		URL tns = Component.class.getClassLoader().getResource(TOPICS_NS_FILE);
		if (tns == null) {
			throw new JBIException(
					"Can not find the notification topicnamespace configuration file");
		}

		String endpointName = props.getProperty(ENDPOINT_NAME);
		QName interfaceName = QName.valueOf(props.getProperty(INTERFACE_NAME));
		QName serviceName = QName.valueOf(props.getProperty(SERVICE_NAME));

		if (engine == null) {
			engine = new NotificationEngine(getLogger(), serviceName,
					interfaceName, endpointName, getClient(), getMonitoringService());
		}
		this.engine.init(topics, tns);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.petals.component.framework.AbstractComponent
	 */
	@Override
	protected void doStart() throws JBIException {
		activateWSNEndpoints();
		createSubscribers();
		startWebServices();
	}

	protected void startWebServices() {
		if (ws != null) {
			int i = 0;
			for (Service service : ws) {
				try {
					getLogger().info("Starting service at " + service.getURL());
					service.start();
					ComponentInformation info = getPlugin(ComponentInformation.class);
					if (info != null) {
						info.addProperty(this.getContext().getComponentName()
								+ "-managementws-" + i++, service.getURL());
					}
				} catch (Exception e) {
					getLogger().warning(e.getMessage());
				}
			}
		}
		getLogger().info("Web services started");
	}

	/**
     * 
     */
	protected void createSubscribers() {
		// create default subscribers, ie automatically subscribe to myself for
		// others...
		// look if we have some configuration about subscribers...
		URL subscribers = Component.class.getClassLoader().getResource(
				"subscribers.cfg");

		Properties subscriberProps = null;
		if (subscribers != null) {
			subscriberProps = new Properties();
			try {
				subscriberProps.load(Component.class.getClassLoader()
						.getResourceAsStream("subscribers.cfg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (subscriberProps != null) {
			ConfigurationProducer producers = new PropertiesConfigurationProducer(
					subscriberProps);
			List<Subscribe> toSubscribe = producers.getSubscribes();
			for (Subscribe subscribe : toSubscribe) {
				// let's subscribe...
				try {

					if (getLogger().isLoggable(Level.INFO)) {
						getLogger().info("Subscribe request : ");
						if (subscribe != null) {
							Document doc = Wsnb4ServUtils.getWsnbWriter()
									.writeSubscribeAsDOM(subscribe);
							getLogger().info(
									XMLHelper.createStringFromDOMDocument(doc));
						}
					}

					final com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse subscribeResponse = getNotificationEngine()
							.getNotificationManager()
							.getNotificationProducerEngine()
							.subscribe(subscribe);

					if (getLogger().isLoggable(Level.INFO)) {
						getLogger().info("Subscribe response");
						if (subscribeResponse != null) {
							Document doc = Wsnb4ServUtils.getWsnbWriter()
									.writeSubscribeResponseAsDOM(
											subscribeResponse);
							getLogger().info(
									XMLHelper.createStringFromDOMDocument(doc));
						} else {
							getLogger().info("No response...");
						}
					}
				} catch (Exception e) {
					if (getLogger().isLoggable(Level.FINE)) {
						getLogger().log(Level.INFO,
								"Problem while subscribing", e);
					}
				}
			}
		}
	}

	/**
	 * @throws JBIException
	 * 
	 */
	private void activateWSNEndpoints() throws JBIException {
		List<Endpoint> endpointList = null;
		QName serviceName = null;
		String endpointName = null;

		endpointList = WSDLUtilImpl.getEndpointList(this.engine
				.getProducerWSDL().getDescription());
		if (endpointList != null) {
			final Iterator<Endpoint> iterator = endpointList.iterator();
			if (iterator != null) {
				while (iterator.hasNext()) {
					final Endpoint endpoint = iterator.next();
					if (endpoint != null) {
						serviceName = endpoint.getService().getQName();
						endpointName = endpoint.getName();
						this.activateWSNEndpoint(serviceName, endpointName,
								this.engine.getProducerWSDL());
					}
				}
			}
		}

		endpointList = WSDLUtilImpl.getEndpointList(this.engine
				.getConsumerWSDL().getDescription());
		if (endpointList != null) {
			final Iterator<Endpoint> iterator = endpointList.iterator();
			if (iterator != null) {
				while (iterator.hasNext()) {
					final Endpoint endpoint = iterator.next();
					if (endpoint != null) {
						serviceName = endpoint.getService().getQName();
						endpointName = endpoint.getName();
						this.activateWSNEndpoint(serviceName, endpointName,
								this.engine.getConsumerWSDL());
					}
				}
			}
		}

	}

	/**
	 * @param serviceName
	 * @param endpointName
	 */
	private void activateWSNEndpoint(final QName serviceName,
			final String endpointName, Wsdl wsdl) throws JBIException {
		// add it before since activate endpoint will call getDescription
		// locally
		this.WSNEP.put(new ServiceEndpointKey(serviceName, endpointName), wsdl);
		try {
			this.context.activateEndpoint(serviceName, endpointName);
		} catch (Exception e) {
			this.WSNEP
					.remove(new ServiceEndpointKey(serviceName, endpointName));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.petals.component.framework.AbstractComponent
	 */
	@Override
	protected void doStop() throws JBIException {
		if (ws != null) {
			for (Service service : ws) {
				try {
					service.stop();
				} catch (Exception e) {
					getLogger().warning(e.getMessage());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.petals.component.framework.AbstractComponent
	 */
	@Override
	protected void doShutdown() throws JBIException {
	}

	// override some deprecated stuff...
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.petals.component.framework.AbstractComponent#getServiceDescription
	 * (javax.jbi.servicedesc.ServiceEndpoint)
	 */
	@Override
	public Document getServiceDescription(ServiceEndpoint endpoint) {
		if (endpoint == null) {
			return null;
		}

		if (isNotification(endpoint)) {
			return getWSNDescription(endpoint);
		}
		return super.getServiceDescription(endpoint);
	}

	/**
	 * @param endpoint
	 * @return
	 */
	private Document getWSNDescription(ServiceEndpoint endpoint) {
		Wsdl desc = WSNEP.get(new ServiceEndpointKey(endpoint.getServiceName(),
				endpoint.getEndpointName()));
		if (desc != null) {
			return desc.getDocument();
		}
		return null;
	}

	public NotificationEngine getNotificationEngine() {
		return engine;
	}

	/**
	 * @param endpoint
	 * @return
	 */
	private boolean isNotification(ServiceEndpoint endpoint) {
		return WSNEP.get(new ServiceEndpointKey(endpoint.getServiceName(),
				endpoint.getEndpointName())) != null;
	}

	protected synchronized Client getClient() {
		// TODO : Set client by configuration
		// Client client = getJBIClient();

		if (httpClient == null) {
			httpClient = new org.petalslink.dsb.service.client.saaj.Client();
		}
		return httpClient;
	}
	
	/**
	 * To be overrided
	 * 
	 * @return
	 */
	protected synchronized MonitoringService getMonitoringService() {
		return null;
	}
}
