package org.laopopo.console.info.kaleidoscope;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.common.metrics.ServiceMetrics;
import org.laopopo.common.metrics.ServiceMetrics.ConsumerInfo;
import org.laopopo.common.metrics.ServiceMetrics.ProviderInfo;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.rpc.ManagerServiceRequestType;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.rpc.ServiceReviewState;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.transport.body.ManagerServiceCustomBody;
import org.laopopo.common.transport.body.MetricsCustomBody;
import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.remoting.model.RemotingTransporter;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 信息收集的万花筒
 * @time 2016年8月17日
 * @modifytime 2016年9月1日
 */
public class KaleidoscopeInfo {
	

	private static final Logger logger = LoggerFactory.getLogger(KaleidoscopeInfo.class);

	private String registryAddress;					 //注册中心的地址，可以多个
	private String monitorAddress;					 //监控中心的地址
	private NettyRemotingClient nettyRemotingClient; // 连接monitor和注册中心

	private static ConcurrentMap<String, ServiceMetrics> globalServiceMetrics = new ConcurrentHashMap<String, ServiceMetrics>();

	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("console-timer"));

	public KaleidoscopeInfo(String registryAddress, String monitorAddress) {
		this.registryAddress = registryAddress;
		this.monitorAddress = monitorAddress;
		initialize();
	}

	private void initialize() {
		
		NettyClientConfig clientConfig = new NettyClientConfig();
		this.nettyRemotingClient = new NettyRemotingClient(clientConfig);

		this.nettyRemotingClient.start();

		logger.info("console init successfully");

		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				// 延迟60秒，每隔60秒开始 定时向所有的注册中心发送请求，获取所有的服务注册信息和对应的订阅者的信息
				try {
					KaleidoscopeInfo.this.getLastRegistryServerInfo();
				} catch (Exception e) {
					logger.warn("schedule get registryInfos from registryServer failed [{}]", e.getMessage());
				}
			}
		}, 3, 60, TimeUnit.SECONDS);

		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				// 延迟60秒，每隔60秒开始 定时向监控中心发送请求，获取所有的服务被调用次数的信息和失败的信息
				try {
					KaleidoscopeInfo.this.refreshMonitorServerInfo();
				} catch (Exception e) {
					logger.warn("schedule get monitorInfos from monitorServer failed [{}]", e.getMessage());
				}
			}
		}, 6, 60, TimeUnit.SECONDS);
	}

	protected void refreshMonitorServerInfo() throws InterruptedException {
		
		Set<String> serviceNames = globalServiceMetrics.keySet();
		if(null != serviceNames && !serviceNames.isEmpty()){
			for(String str:serviceNames){
				getLastMonitorServerInfo(str);
				//分批次发送
				Thread.sleep(300l);
			}
		}
	}

	/**
	 * 从监控中心端获取到最新的统计信息，更新信息
	 * 这个方法的细节是将要统计的服务名发送给监控中心，监控中心返回这个服务的统计信息
	 * 将得到的信息更新到本地的map中
	 * @param serviceName
	 */
	protected void getLastMonitorServerInfo(String serviceName) {

		// 当监控端口的URL不是空的时候发送统计信息
		if (this.monitorAddress != null && serviceName != null) {
			
			ManagerServiceCustomBody managerServiceCustomBody = new ManagerServiceCustomBody();
			// 设置属性为==>统计
			managerServiceCustomBody.setManagerServiceRequestType(ManagerServiceRequestType.METRICS);
			//统计的服务名
			managerServiceCustomBody.setSerivceName(serviceName);
			RemotingTransporter requestTransporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.MANAGER_SERVICE, managerServiceCustomBody);

			//监控中心返回的结果
			RemotingTransporter responseTransporter = null;
			try {
				
				responseTransporter = this.nettyRemotingClient.invokeSync(monitorAddress, requestTransporter, 3000l);
				
				MetricsCustomBody registryMetricsCustomBody = serializerImpl().readObject(responseTransporter.bytes(), MetricsCustomBody.class);

				List<ServiceMetrics> serviceMetricses = registryMetricsCustomBody.getServiceMetricses();
				if (serviceMetricses != null && !serviceMetricses.isEmpty()) {
					//因为只发送了一个服务名，所以只返回一个统计结果
					ServiceMetrics metrics = serviceMetricses.get(0);

					synchronized (globalServiceMetrics) { //lock
						
						//获取到本地的统计信息
						ServiceMetrics serviceMetrics = globalServiceMetrics.get(serviceName);
						//更新统计信息
						serviceMetrics.setHandlerAvgTime(metrics.getHandlerAvgTime()); //平均请求时间
						serviceMetrics.setTotalCallCount(metrics.getTotalCallCount()); //成功请求的次数
						serviceMetrics.setTotalFailCount(metrics.getTotalFailCount()); //失败请求的次数
						//更新每个提供者的信息
						ConcurrentMap<Address, ProviderInfo> existMap = serviceMetrics.getProviderMaps(); //本地的提供者的信息
						ConcurrentMap<Address, ProviderInfo> remotingMap = metrics.getProviderMaps();
						for (Address address : existMap.keySet()) {

							if (remotingMap.containsKey(address)) {
								
								ProviderInfo existInfo = existMap.get(address);
								if (null != existInfo && remotingMap.get(address) != null) {
									ProviderInfo remotingInfo = remotingMap.get(address);
									existInfo.setCallCount(remotingInfo.getCallCount());
									existInfo.setFailCount(remotingInfo.getFailCount());
									existInfo.setHandlerAvgTime(remotingInfo.getHandlerAvgTime());
								}
							}
						}
					}
				}

			} catch (InterruptedException | RemotingException e) {
				logger.error("connection to monitor error address [{}] and exception [{}]", monitorAddress, e.getMessage());
			}

		}
	}

	/**
	 * 从注册中心(多个)中更新信息
	 */
	protected void getLastRegistryServerInfo() {

		if (this.registryAddress != null) {

			ManagerServiceCustomBody managerServiceCustomBody = new ManagerServiceCustomBody();
			// 设置属性为==>统计
			managerServiceCustomBody.setManagerServiceRequestType(ManagerServiceRequestType.METRICS);
			RemotingTransporter requestTransporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.MANAGER_SERVICE, managerServiceCustomBody);

			String[] registryAddresses = this.registryAddress.split(",");

			for (int index = 0; index < registryAddresses.length; index++) {
				try {

					RemotingTransporter responseTransporter = this.nettyRemotingClient.invokeSync(registryAddresses[index], requestTransporter, 3000l);
					MetricsCustomBody registryMetricsCustomBody = serializerImpl().readObject(responseTransporter.bytes(), MetricsCustomBody.class);

					List<ServiceMetrics> serviceMetricses = registryMetricsCustomBody.getServiceMetricses();

					logger.info("response from registry address [{}] reveice info size [{}]", registryAddresses[index], serviceMetricses == null ? 0
							: serviceMetricses.size());

					if (null != serviceMetricses && !serviceMetricses.isEmpty()) {

						synchronized (globalServiceMetrics) {
							
							for (ServiceMetrics serviceMetrics : serviceMetricses) {

								logger.info("ServiceMetrics [{}]", serviceMetrics);

								ServiceMetrics currentServiceMetrics = globalServiceMetrics.get(serviceMetrics.getServiceName());

								if (currentServiceMetrics == null) {
									currentServiceMetrics = new ServiceMetrics();
									globalServiceMetrics.put(serviceMetrics.getServiceName(), currentServiceMetrics);
								}
								// 更新负载均衡策略
								currentServiceMetrics.setLoadBalanceStrategy(serviceMetrics.getLoadBalanceStrategy());
								currentServiceMetrics.setServiceName(serviceMetrics.getServiceName());
								Set<ConsumerInfo> consumerInfos = currentServiceMetrics.getConsumerInfos();
								ConcurrentMap<Address, ProviderInfo> providerInfos = currentServiceMetrics.getProviderMaps();
								// 如果是某个更新批次的第一批次，则将过去更新的信息清除掉
								if (index == 0) {
									consumerInfos.clear();
									providerInfos.clear();
								}
								consumerInfos.addAll(serviceMetrics.getConsumerInfos());
								// 合并2个map
								ConcurrentMap<Address, ProviderInfo> concurrentMap = serviceMetrics.getProviderMaps();
								if (null != concurrentMap && !concurrentMap.keySet().isEmpty()) {
									for (Address address : concurrentMap.keySet()) {
										providerInfos.put(address, concurrentMap.get(address));
									}
								}
							}
						}
					}
				} catch (InterruptedException | RemotingException e) {
					logger.error("connection to registry address[{}] failed,and exception is [{}]", registryAddresses[index],e.getMessage());
				}
			}

		}
	}
	
	public Map<String, Object> findInfoByPage(int pageSize, int offset) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		Integer total = 0;
		List<ServiceMetrics> serviceMetrics = new ArrayList<ServiceMetrics>();
		Collection<ServiceMetrics> serviceMetricses = globalServiceMetrics.values();
		if (null != serviceMetricses) {
			serviceMetrics.addAll(serviceMetricses);
			total = serviceMetrics.size();
		}

		resultMap.put("status", "success");
		resultMap.put("total", total);
		resultMap.put("rows", serviceMetrics != null ? serviceMetrics.subList(offset, Math.min((offset + pageSize), total)) : new ArrayList<ServiceMetrics>());
		return resultMap;
	}

	public Boolean notifyServiceForbidden(String host, int port, String serviceName) {
		
		ManagerServiceCustomBody managerServiceCustomBody = new ManagerServiceCustomBody();
		// 设置属性为==>审核
		managerServiceCustomBody.setManagerServiceRequestType(ManagerServiceRequestType.REVIEW);
		managerServiceCustomBody.setAddress(new Address(host, port));
		managerServiceCustomBody.setSerivceName(serviceName);
		managerServiceCustomBody.setServiceReviewState(ServiceReviewState.FORBIDDEN);
		RemotingTransporter requestTransporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.MANAGER_SERVICE, managerServiceCustomBody);
		
		boolean successFlag = true;
		
		if(this.registryAddress != null){
			
        String[] registryAddresses = this.registryAddress.split(",");
        
			for (int index = 0; index < registryAddresses.length; index++) {
				
				try {
					RemotingTransporter responseTransporter = this.nettyRemotingClient.invokeSync(registryAddresses[index], requestTransporter, 3000l);
					AckCustomBody ackCustomBody = serializerImpl().readObject(responseTransporter.bytes(), AckCustomBody.class);
					successFlag = successFlag && ackCustomBody.isSuccess();
				} catch (InterruptedException | RemotingException e) {
					logger.error("send notify fail serviceName[{}] and exception [{}]",serviceName,e.getMessage());
					successFlag = false;
				}
				
			}
		}else{
			successFlag = false;
		}
		return successFlag;
	}
	
	public Boolean notifyServiceDegrade(String host, int port, String serviceName) {
		
		ManagerServiceCustomBody managerServiceCustomBody = new ManagerServiceCustomBody();
		// 设置属性为==>审核
		managerServiceCustomBody.setManagerServiceRequestType(ManagerServiceRequestType.DEGRADE);
		managerServiceCustomBody.setAddress(new Address(host, port));
		managerServiceCustomBody.setSerivceName(serviceName);
		RemotingTransporter requestTransporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.MANAGER_SERVICE, managerServiceCustomBody);
		
		boolean successFlag = true;
		
		if(this.registryAddress != null){
			
	        String[] registryAddresses = this.registryAddress.split(",");
	        
				for (int index = 0; index < registryAddresses.length; index++) {
					
					try {
						RemotingTransporter responseTransporter = this.nettyRemotingClient.invokeSync(registryAddresses[index], requestTransporter, 3000l);
						AckCustomBody ackCustomBody = serializerImpl().readObject(responseTransporter.bytes(), AckCustomBody.class);
						successFlag = successFlag && ackCustomBody.isSuccess();
					} catch (InterruptedException | RemotingException e) {
						logger.error("send notify fail serviceName[{}] and exception [{}]",serviceName,e.getMessage());
						successFlag = false;
					}
					
				}
			}else{
				successFlag = false;
			}
		return null;
	}

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public String getMonitorAddress() {
		return monitorAddress;
	}

	public void setMonitorAddress(String monitorAddress) {
		this.monitorAddress = monitorAddress;
	}

	

}
