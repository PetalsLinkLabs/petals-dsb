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
package org.petalslink.dsb.jbi.se.wsn.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.petalslink.dsb.jbi.se.wsn.Stats;
import org.petalslink.dsb.jbi.se.wsn.api.StatsService;
import org.petalslink.dsb.jbi.se.wsn.api.Topic;
import org.petalslink.dsb.jbi.se.wsn.api.TopicStat;

/**
 * @author chamerling
 * 
 */
public class StatsServiceImpl implements StatsService {
    
    private Logger logger;
    
    public StatsServiceImpl(Logger logger) {
        this.logger = logger;
    }

    /**
     * 
     */
    public List<Topic> getTopics() {
        logger.info("Get topics");
        return new ArrayList<Topic>();
    }

    /**
     * 
     */
    public long getNbInNotifyMessage(Topic topic) {
        logger.fine("getInNbNotifyMessage for topic " + topic);
        return Stats.getInstance().getInNotifyPerTopic().get(topic) == null ? 0l : Stats
                .getInstance().getInNotifyPerTopic().get(topic).get();
    }
    
    /**
     * 
     */
    public long getNbOutNotifyMessage(Topic topic) {
        logger.fine("getOutNbNotifyMessage for topic " + topic);
        return Stats.getInstance().getInNotifyPerTopic().get(topic) == null ? 0l : Stats
                .getInstance().getInNotifyPerTopic().get(topic).get();
    }

    /**
     * 
     */
    public long getNbSubscribeMessage(Topic topic) {
        logger.fine("getNbSubscribeMessage for topic " + topic);
        return 0;
    }

    public List<TopicStat> getInStats() {
        logger.fine("getInStats");

        Map<Topic, AtomicLong> stats = Stats.getInstance().getInNotifyPerTopic();
        List<TopicStat> result = new ArrayList<TopicStat>();

        Iterator<Entry<Topic, AtomicLong>> iter = stats.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Topic, AtomicLong> e = iter.next();
            TopicStat stat = new TopicStat();
            stat.calls = e.getValue().get();
            stat.topic = e.getKey();
            result.add(stat);
        }
        return result;
    }
    
    public List<TopicStat> getOutStats() {
        logger.fine("getInStats");

        Map<Topic, AtomicLong> stats = Stats.getInstance().getOutNotifyPerTopic();
        List<TopicStat> result = new ArrayList<TopicStat>();

        Iterator<Entry<Topic, AtomicLong>> iter = stats.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Topic, AtomicLong> e = iter.next();
            TopicStat stat = new TopicStat();
            stat.calls = e.getValue().get();
            stat.topic = e.getKey();
            result.add(stat);
        }
        return result;
    }

}
