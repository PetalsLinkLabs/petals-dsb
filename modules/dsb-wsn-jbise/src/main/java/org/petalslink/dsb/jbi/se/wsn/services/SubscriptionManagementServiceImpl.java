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
package org.petalslink.dsb.jbi.se.wsn.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.petalslink.dsb.jbi.se.wsn.NotificationEngine;
import org.petalslink.dsb.jbi.se.wsn.api.Subscription;
import org.petalslink.dsb.jbi.se.wsn.api.SubscriptionManagementService;
import org.petalslink.dsb.jbi.se.wsn.api.Topic;
import org.petalslink.dsb.jbi.se.wsn.api.WSNException;

import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.wsresources.WsnSubscription;

/**
 * @author chamerling
 * 
 */
public class SubscriptionManagementServiceImpl implements
		SubscriptionManagementService {

	private Logger logger;

	private NotificationEngine engine;

	/**
	 * 
	 */
	public SubscriptionManagementServiceImpl(NotificationEngine engine,
			Logger logger) {
		this.logger = logger;
		this.engine = engine;
	}

	public List<Subscription> getSubscriptionsForTopic(Topic topic)
			throws WSNException {
		logger.info("DSB WSB SE : Get subscriptions for topic " + topic);

		List<Subscription> result = new ArrayList<Subscription>();
		List<WsnSubscription> subscriptions = this.engine
				.getNotificationManager()
				.getSubscriptionManagerEngine()
				.getSubscriptions(new QName(topic.ns, topic.name, topic.prefix));
		
		if (subscriptions != null) {
			for (WsnSubscription wsnSubscription : subscriptions) {
				Subscription s = toSubscription(wsnSubscription);
				s.topic = topic;
				result.add(s);
			}
		}
		return result;
	}

	public List<Subscription> getSubscriptions() throws WSNException {
		logger.info("DSB WSB SE : Get subscriptions");

		List<Subscription> result = new ArrayList<Subscription>();
		
		List<Topic> topics = this.engine.getTopics();
		if (topics == null) {
			return result;
		}
		
		for (Topic topic : topics) {
			result.addAll(getSubscriptionsForTopic(topic));
		}
		return result;
	}

	/**
	 * 
	 * @param wsnSubscription
	 * @return
	 */
	private Subscription toSubscription(WsnSubscription wsnSubscription) {
		Subscription result = new Subscription();

		if (wsnSubscription == null) {
			return result;
		}

		try {
			if (wsnSubscription.getConsumerEdpRef() != null
					&& wsnSubscription.getConsumerEdpRef().getAddress() != null) {
				result.subscriber = wsnSubscription.getConsumerEdpRef()
						.getAddress().getValue().toString();
			}
		} catch (WsnbException e) {
			logger.warning(e.getMessage());
		}

		result.uuid = Wsnb4ServUtils
				.getSubscriptionIdFromReferenceParams(wsnSubscription
						.getSubscriptionReference().getReferenceParameters());

		return result;
	}
}
