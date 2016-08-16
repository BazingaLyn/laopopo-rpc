package org.laopopo.base.registry;

import java.util.concurrent.ConcurrentHashMap;

import org.laopopo.base.registry.model.ReviewRecord;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.RequestReviewCustomBody;
import org.laopopo.common.transport.body.ReviewResultCustomBody;
import org.laopopo.registry.model.RegisterMeta;
import org.laopopo.remoting.model.RemotingTransporter;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 与监控端通信的管理工具控制类
 * @time 2016年8月15日
 * @modifytime
 */
public class MonitorConfigManager {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorConfigManager.class);
	
	//TODO isNeed
	private DefaultRegistryServer defaultRegistryServer;
	
	private NettyRemotingClient nettyRemotingClient;
	
	private NettyClientConfig linkMonitorClientConfig;
	
	private ConcurrentHashMap<String, ReviewRecord> globeReviewMeta = new ConcurrentHashMap<String, ReviewRecord>();
	
	public MonitorConfigManager(DefaultRegistryServer defaultRegistryServer, NettyRemotingClient nettyRemotingClient,NettyClientConfig linkMonitorClientConfig) {
		this.defaultRegistryServer = defaultRegistryServer;
		this.nettyRemotingClient = nettyRemotingClient;
		this.linkMonitorClientConfig = linkMonitorClientConfig;
	}

	public boolean isReviewed(RegisterMeta meta) {
		//某个服务的唯一标识 
		String completeServiceName  = meta.getServiceMeta().getServiceProviderName() + "-" +
	                                  meta.getServiceMeta().getGroup() + "-" + 
				                      meta.getServiceMeta().getVersion() + "-" + 
	                                  meta.getAddress().getHost() +"-"+ 
				                      meta.getAddress().getPort() +"-" + (meta.getServiceMeta().isVIPService() ? "VIP" :"COMMON");
		ReviewRecord record = globeReviewMeta.get(completeServiceName);
		//如果还没有审核记录的情况下，发送审核请求
		if(null == record){
			try {
				record = sendReviewRequest(meta);
				globeReviewMeta.put(completeServiceName, record);
			} catch (InterruptedException | RemotingException e) {
				logger.error("send review Request occor exception [{}]",e.getMessage());
				globeReviewMeta.put(completeServiceName, new ReviewRecord());
			}
		}
		return record == null ? false : record.isReviewPass();
	}

	private ReviewRecord sendReviewRequest(RegisterMeta meta) throws InterruptedException, RemotingException {
		
		ReviewRecord record = new ReviewRecord();
		
		RequestReviewCustomBody commonCustomHeader = 
				new RequestReviewCustomBody(meta.getAddress().getHost(), //
														meta.getAddress().getPort(), meta.getServiceMeta().getGroup(),//
														meta.getServiceMeta().getVersion(), //
														meta.getServiceMeta().getServiceProviderName(),//
														meta.getServiceMeta().isVIPService());
		
		
		RemotingTransporter request = RemotingTransporter.createRequestTransporter(LaopopoProtocol.REQUEST_REVIEW_RESULT, commonCustomHeader, LaopopoProtocol.REQUEST_REMOTING);
		RemotingTransporter result = nettyRemotingClient.invokeSync(linkMonitorClientConfig.getDefaultAddress(), request, 3000);
		
		if(null != result){
			if(result.getCustomHeader() instanceof ReviewResultCustomBody){
				ReviewResultCustomBody body = (ReviewResultCustomBody)result.getCustomHeader();
				record.setReviewPass(body.isReviewPass());
				record.setDegradeService(body.isDegradeService());
				record.setWeightVal(body.getWeightVal());
			}
		}
		return record;
	}

}
