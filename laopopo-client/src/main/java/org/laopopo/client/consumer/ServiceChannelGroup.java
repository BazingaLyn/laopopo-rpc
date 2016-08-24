package org.laopopo.client.consumer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.laopopo.common.utils.ChannelGroup;


public class ServiceChannelGroup {
	
	private static final ConcurrentMap<String, CopyOnWriteArrayList<ChannelGroup>> groups = new ConcurrentHashMap<String, CopyOnWriteArrayList<ChannelGroup>>();
	

	public static void addIfAbsent(String serviceName, ChannelGroup group) {
		String _serviceName = serviceName;
		CopyOnWriteArrayList<ChannelGroup> groupList = groups.get(_serviceName);
        if (groupList == null) {
        	CopyOnWriteArrayList<ChannelGroup> newGroupList = new CopyOnWriteArrayList<ChannelGroup>();
            groupList = groups.putIfAbsent(_serviceName, newGroupList);
            if (groupList == null) {
                groupList = newGroupList;
            }
        }
        groupList.addIfAbsent(group);
	}
	
	public static void removedIfAbsent(String serviceName, ChannelGroup group) {
		String _serviceName = serviceName;
		CopyOnWriteArrayList<ChannelGroup> groupList = groups.get(_serviceName);
        if (groupList == null) {
        	return;
        }
        groupList.remove(group);
	}
	
	public static CopyOnWriteArrayList<ChannelGroup> getChannelGroupByServiceName(String service){
		return groups.get(service);
	}
	
	

}
