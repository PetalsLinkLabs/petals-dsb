/**
 * 
 */
package org.petalslink.dsb.easierbsm.wsnconnector;

import javax.xml.namespace.QName;

/**
 * @author chamerling
 * 
 */
public interface EasierBSMConstants {

    static final QName CREATION_TOPIC = new QName("http://www.petalslink.org/resources/event/1.0",
            "CreationResourcesTopic", "bsm");

    static final QName RAWREPORT_TOPIC = new QName("http://www.petalslink.org/rawreport/1.0",
            "RawReportTopic", "bsm");

}
