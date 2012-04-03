/**
 * 
 */
package org.petalslink.dsb.kernel.esmanagement;

import javax.xml.namespace.QName;

import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rpw_2.InvalidResourcePropertyQNameFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnavailableFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Notify;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Subscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.SubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.Unsubscribe;
import com.ebmwebsourcing.wsstar.jaxb.notification.base.UnsubscribeResponse;
import com.ebmwebsourcing.wsstar.jaxb.resource.resourceproperties.GetResourcePropertyResponse;
import com.ebmwebsourcing.wsstar.wsrfbf.services.faults.AbsWSStarFault;

/**
 * @author chamerling
 * 
 */
public class NotificationProxy {

    public static SubscribeResponse subscribe(Subscribe subscribe)
            throws UnacceptableInitialTerminationTimeFault, TopicExpressionDialectUnknownFault,
            InvalidTopicExpressionFault, NotifyMessageNotSupportedFault, TopicNotSupportedFault,
            UnsupportedPolicyRequestFault, ResourceUnknownFault, InvalidFilterFault,
            InvalidProducerPropertiesExpressionFault, UnrecognizedPolicyRequestFault,
            InvalidMessageContentExpressionFault, SubscribeCreationFailedFault {

        org.petalslink.dsb.kernel.pubsub.service.NotificationCenter notificationCenter = org.petalslink.dsb.kernel.pubsub.service.NotificationCenter
                .get();
        if (notificationCenter != null) {

            com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe subs = null;

            // TODO
            try {
                com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse resp = notificationCenter
                        .getManager().getNotificationProducerEngine().subscribe(subs);
                if (resp != null) {
                    SubscribeResponse response = null;
                    // TODO
                    return response;
                }
            } catch (WsnbException e) {
                e.printStackTrace();
            } catch (AbsWSStarFault e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Can not find the notification center to send subscribe to");
        }

        return null;
    }

    public static UnsubscribeResponse unsubscribe(Unsubscribe unsubscribe)
            throws UnableToDestroySubscriptionFault, ResourceUnknownFault {

        org.petalslink.dsb.kernel.pubsub.service.NotificationCenter notificationCenter = org.petalslink.dsb.kernel.pubsub.service.NotificationCenter
                .get();
        if (notificationCenter != null) {

            com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Unsubscribe u = null;

            // TODO
            try {
                com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.UnsubscribeResponse resp = notificationCenter
                        .getManager().getSubscriptionManagerEngine().unsubscribe(u);
                if (resp != null) {
                    UnsubscribeResponse response = null;
                    // TODO
                    return response;
                }
            } catch (WsnbException e) {
                e.printStackTrace();
            } catch (AbsWSStarFault e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException(
                    "Can not find the notification center to send unsubscribe to");
        }

        return null;
    }

    public static void notify(Notify arg0) {
        System.out.println("THIS NOTIFY OPERATION IS NOT IMPLEMENTED!!!");

    }

    public static GetResourcePropertyResponse getResourceProperty(QName name)
            throws ResourceUnavailableFault, InvalidResourcePropertyQNameFault,
            ResourceUnknownFault {
        org.petalslink.dsb.kernel.pubsub.service.NotificationCenter notificationCenter = org.petalslink.dsb.kernel.pubsub.service.NotificationCenter
                .get();
        if (notificationCenter != null) {

            try {
                com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse resp = notificationCenter
                        .getManager().getNotificationProducerEngine().getResourceProperty(name);
                if (resp != null) {
                    GetResourcePropertyResponse response = null;
                    // TODO
                    return response;
                }
            } catch (WsnbException e) {
                e.printStackTrace();
            } catch (AbsWSStarFault e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException(
                    "Can not find the notification center to send getResourceProperty to");
        }
        return null;
    }
}
