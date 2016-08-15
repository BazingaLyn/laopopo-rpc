package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description 监控端返回给注册中心的信息
 * @time
 * @modifytime
 */
public class ReviewResultCustomBody implements CommonCustomBody {
	
	/**
	 * 该服务是否通过
	 */
	private boolean isReviewPass;
	
	/**
	 * 该服务是否降级
	 */
	private boolean isDegradeService;
	
	/**
	 * 该服务的权重
	 */
	private int weightVal;
	
	

	public ReviewResultCustomBody(boolean isReviewPass, boolean isDegradeService, int weightVal) {
		this.isReviewPass = isReviewPass;
		this.isDegradeService = isDegradeService;
		this.weightVal = weightVal;
	}



	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}


	public boolean isReviewPass() {
		return isReviewPass;
	}


	public void setReviewPass(boolean isReviewPass) {
		this.isReviewPass = isReviewPass;
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
