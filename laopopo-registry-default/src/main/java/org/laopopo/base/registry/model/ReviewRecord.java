package org.laopopo.base.registry.model;

/**
 * 某个服务对应的审核记录
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class ReviewRecord {

	private boolean isReviewPass;
	
	/**
	 * 该服务是否降级
	 */
	private boolean isDegradeService;
	
	/**
	 * 该服务的权重
	 */
	private int weightVal;

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
