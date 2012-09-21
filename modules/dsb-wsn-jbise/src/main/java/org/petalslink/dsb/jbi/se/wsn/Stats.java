/**
 * 
 * Copyright (c) 2012, PetalsLink
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 */
package org.petalslink.dsb.jbi.se.wsn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.namespace.QName;

import org.petalslink.dsb.jbi.se.wsn.api.Topic;

/**
 * @author chamerling
 * 
 */
public class Stats {

    private static Stats instance;

    /**
     * Total nb of calls
     */
    private AtomicLong calls = new AtomicLong(0);

    private Map<Topic, AtomicLong> inNotifyPerTopic;

    private Map<Topic, AtomicLong> outNotifyPerTopic;
    
    private Set<Topic> topics;

    private Stats() {
        init();
    }

    public static synchronized Stats getInstance() {
        if (instance == null) {
            instance = new Stats();
        }
        return instance;
    }

    public void init() {
        // not synchronized...
        calls = new AtomicLong(0);
        inNotifyPerTopic = new ConcurrentHashMap<Topic, AtomicLong>();
        outNotifyPerTopic = new ConcurrentHashMap<Topic, AtomicLong>();
        topics = new HashSet<Topic>();
    }

    public long getCalls() {
        return calls.get();
    }

    public void newInNotifyCall(QName topic) {
        if (topic == null) {
            return;
        }

        Topic t = topic(topic);
        topics.add(t);

        if (inNotifyPerTopic.get(t) == null) {
            inNotifyPerTopic.put(t, new AtomicLong(0));
        }
        inNotifyPerTopic.get(t).incrementAndGet();
    }

    public void newOutNotifyCall(QName topic) {
        if (topic == null) {
            return;
        }

        Topic t = topic(topic);
        topics.add(t);

        if (outNotifyPerTopic.get(t) == null) {
            outNotifyPerTopic.put(t, new AtomicLong(0));
        }
        outNotifyPerTopic.get(t).incrementAndGet();
    }

    public Map<Topic, AtomicLong> getInNotifyPerTopic() {
        return inNotifyPerTopic;
    }

    public Map<Topic, AtomicLong> getOutNotifyPerTopic() {
        return outNotifyPerTopic;
    }

    private static final Topic topic(QName topic) {
        Topic t = new Topic();
        t.name = topic.getLocalPart();
        t.ns = topic.getNamespaceURI();
        t.prefix = topic.getPrefix();
        return t;
    }
}
