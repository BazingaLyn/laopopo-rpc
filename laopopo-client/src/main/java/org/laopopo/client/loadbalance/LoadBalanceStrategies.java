package org.laopopo.client.loadbalance;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.laopopo.common.utils.ChannelGroup;

/**
 * 
 * @author BazingaLyn
 * @description 负载均衡算法
 * @time 2016年9月1日17:48:47
 * @modifytime 2016年9月8日
 */
public enum LoadBalanceStrategies {
	
	//随机
	RANDOMSTRATEGIES(new LoadBalance(){

		@Override
		public ChannelGroup select(CopyOnWriteArrayList<ChannelGroup> arrayList) {
			Random random = new Random();
			int randomPos = random.nextInt(arrayList.size());
			
			return arrayList.get(randomPos);
		}
		
	}),
	
	//加权随机
	WEIGHTRANDOMSTRATEGIES(new LoadBalance(){

		@Override
		public ChannelGroup select(CopyOnWriteArrayList<ChannelGroup> arrayList) {
			int count = arrayList.size();
			if (count == 0) {
				throw new IllegalArgumentException("empty elements for select");
			}
			if (count == 1) {
				return arrayList.get(0);
			}
			int totalWeight = 0;
			int[] weightSnapshots = new int[count];
			for (int i = 0; i < count; i++) {
				totalWeight += (weightSnapshots[i] = getWeight(arrayList.get(i)));
			}

			boolean allSameWeight = true;
			for (int i = 1; i < count; i++) {
				if (weightSnapshots[0] != weightSnapshots[i]) {
					allSameWeight = false;
					break;
				}
			}

			ThreadLocalRandom random = ThreadLocalRandom.current();
			// 如果权重不相同且总权重大于0, 则按总权重数随机
			if (!allSameWeight && totalWeight > 0) {
				int offset = random.nextInt(totalWeight);
				// 确定随机值落在哪个片
				for (int i = 0; i < count; i++) {
					offset -= weightSnapshots[i];
					if (offset < 0) {
						return arrayList.get(i);
					}
				}
			}

			return (ChannelGroup) arrayList.get(random.nextInt(count));
		}
		
		private int getWeight(ChannelGroup channelGroup) {
			return channelGroup.getWeight();
		}
		
	}), 
	
	ROUNDROBIN(new LoadBalance(){
		
		AtomicInteger position = new AtomicInteger(0);

		@Override
		public ChannelGroup select(CopyOnWriteArrayList<ChannelGroup> arrayList) {
			int count = arrayList.size();
			if (count == 0) {
				throw new IllegalArgumentException("empty elements for select");
			}
			if (count == 1) {
				return arrayList.get(0);
			}
			
			ChannelGroup channelGroup = arrayList.get(position.getAndIncrement() / count);
			
			return channelGroup;

		}
		
		
	});
	
	
	private final LoadBalance loadBalance;
	
	
	LoadBalanceStrategies(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
	
	public ChannelGroup select(CopyOnWriteArrayList<ChannelGroup> arrayList){
		return loadBalance.select(arrayList);
	}
	
	interface LoadBalance {
		
		ChannelGroup select(CopyOnWriteArrayList<ChannelGroup> arrayList);
	}
	
	

}
