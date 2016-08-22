package org.laopopo.client.consumer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.laopopo.client.consumer.ConsumerRegistry.SubcribeService;
import org.laopopo.common.utils.ChannelGroup;


public class ServiceChannelGroup {
	
	private static final ConcurrentMap<SubcribeService, CopyOnWriteArrayList<ChannelGroup>> groups = new ConcurrentHashMap<SubcribeService, CopyOnWriteArrayList<ChannelGroup>>();
	

	public static void addIfAbsent(SubcribeService subcribeService, ChannelGroup group) {
		CopyOnWriteArrayList<ChannelGroup> groupList = groups.get(subcribeService);
        if (groupList == null) {
        	CopyOnWriteArrayList<ChannelGroup> newGroupList = new CopyOnWriteArrayList<ChannelGroup>();
            groupList = groups.putIfAbsent(subcribeService, newGroupList);
            if (groupList == null) {
                groupList = newGroupList;
            }
        }
        groupList.addIfAbsent(group);
	}
	
	public static void removedIfAbsent(SubcribeService subcribeService, ChannelGroup group) {
		CopyOnWriteArrayList<ChannelGroup> groupList = groups.get(subcribeService);
        if (groupList == null) {
        	return;
        }
        groupList.remove(group);
	}
	
	

}
