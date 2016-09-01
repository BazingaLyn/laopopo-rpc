package org.laopopo.client.consumer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.laopopo.client.loadbalance.LoadBalanceStrategies;
import org.laopopo.common.loadbalance.LoadBalanceStrategy;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.common.utils.UnresolvedAddress;

/**
 * 
 * @author BazingaLyn
 * @description 消费端的抽象类，这个类的意义：
 * 1)保存从注册中心获取到的每个服务的提供者的信息
 * 2)保存每一个服务的负载均衡的策略
 * @time 2016年9月1日
 * @modifytime
 */
public abstract class AbstractDefaultConsumer implements Consumer {

	/******key是服务名，Value该服务对应的提供的channel的信息集合************/
	private volatile static ConcurrentMap<String, CopyOnWriteArrayList<ChannelGroup>> groups = new ConcurrentHashMap<String, CopyOnWriteArrayList<ChannelGroup>>();
	/***********某个服务提供者的地址对应的channelGroup*************/
	protected final ConcurrentMap<UnresolvedAddress, ChannelGroup> addressGroups = new ConcurrentHashMap<UnresolvedAddress, ChannelGroup>();
	/*********************某个服务对应的负载均衡的策略***************/
	protected final ConcurrentHashMap<String, LoadBalanceStrategy> loadConcurrentHashMap = new ConcurrentHashMap<String, LoadBalanceStrategy>();
	/**
	 * 为某个服务增加一个ChannelGroup
	 * @param serviceName
	 * @param group
	 */
	public static boolean addIfAbsent(String serviceName, ChannelGroup group) {
		String _serviceName = serviceName;
		CopyOnWriteArrayList<ChannelGroup> groupList = groups.get(_serviceName);
		if (groupList == null) {
			CopyOnWriteArrayList<ChannelGroup> newGroupList = new CopyOnWriteArrayList<ChannelGroup>();
			groupList = groups.putIfAbsent(_serviceName, newGroupList);
			if (groupList == null) {
				groupList = newGroupList;
			}
		}
		return groupList.addIfAbsent(group);
	}

	/**
	 * 当某个group 失效或者下线的时候，将其冲value中移除
	 * @param serviceName
	 * @param group
	 */
	public static boolean removedIfAbsent(String serviceName, ChannelGroup group) {
		String _serviceName = serviceName;
		CopyOnWriteArrayList<ChannelGroup> groupList = groups.get(_serviceName);
		if (groupList == null) {
			return false;
		}
		return groupList.remove(group);
	}

	public static CopyOnWriteArrayList<ChannelGroup> getChannelGroupByServiceName(String service) {
		return groups.get(service);
	}
	
	@Override
	public void setServiceLoadBalanceStrategy(String serviceName, LoadBalanceStrategy loadBalanceStrategy) {
		LoadBalanceStrategy balanceStrategy = loadConcurrentHashMap.get(serviceName);
		if(null == balanceStrategy){
			balanceStrategy = LoadBalanceStrategy.WEIGHTINGRANDOM;
			loadConcurrentHashMap.put(serviceName, balanceStrategy);
		}
		balanceStrategy = loadBalanceStrategy;
	}

	
	public static ConcurrentMap<String, CopyOnWriteArrayList<ChannelGroup>> getGroups() {
		return groups;
	}
	
	@Override
	public ChannelGroup loadBalance(String serviceName) {
		LoadBalanceStrategy balanceStrategy = loadConcurrentHashMap.get(serviceName);
		
		CopyOnWriteArrayList<ChannelGroup> list = groups.get(serviceName);
		if(balanceStrategy == null){
			balanceStrategy = LoadBalanceStrategy.WEIGHTINGRANDOM;
		}
		
		if(null == list || list.size() == 0){
			return null;
		}
		switch (balanceStrategy) {
		case RANDOM:
			return LoadBalanceStrategies.RANDOMSTRATEGIES.select(list);
		case WEIGHTINGRANDOM:
			return LoadBalanceStrategies.WEIGHTRANDOMSTRATEGIES.select(list);
		case ROUNDROBIN: //TODO
			return LoadBalanceStrategies.WEIGHTRANDOMSTRATEGIES.select(list);
		default:
			break;
		}
		return null;
	}

}
