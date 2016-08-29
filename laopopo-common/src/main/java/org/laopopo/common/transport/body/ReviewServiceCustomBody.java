package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.rpc.ServiceReviewState;

/**
 * 
 * @author BazingaLyn
 * @description 管理者发送给注册中心的审核请求的主体信息
 * @time 2016年8月29日
 * @modifytime
 */
public class ReviewServiceCustomBody implements CommonCustomBody {
	
	//服务名
	private String serivceName;
	
	//该服务提供的地址
	private Address address;
	
	//是否审核通过
	private ServiceReviewState serviceReviewState;
	
	//该服务是否降级
	private boolean isDegradeService;
	
	//该服务的权重
	private int weightVal;

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}

	public String getSerivceName() {
		return serivceName;
	}

	public void setSerivceName(String serivceName) {
		this.serivceName = serivceName;
	}

	public ServiceReviewState getServiceReviewState() {
		return serviceReviewState;
	}

	public void setServiceReviewState(ServiceReviewState serviceReviewState) {
		this.serviceReviewState = serviceReviewState;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public boolean isDegradeService() {
		return isDegradeService;
	}

	public void setDegradeService(boolean isDegradeService) {
		this.isDegradeService = isDegradeService;
	}

	public int getWeightVal() {
		return weightVal;
	}

	public void setWeightVal(int weightVal) {
		this.weightVal = weightVal;
	}
	

}
