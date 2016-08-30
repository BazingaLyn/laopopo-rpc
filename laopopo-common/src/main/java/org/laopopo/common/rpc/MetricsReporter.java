package org.laopopo.common.rpc;

import java.io.Serializable;


/**
 * 
 * @author BazingaLyn
 * @description 统计报告
 * @time 2016年8月30日
 * @modifytime
 */
public class MetricsReporter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3846340000197732373L;
	
	
	private String serviceName;        //统计的服务名
	private Long callCount;            //调用的次数
	private Long failCount;            //失败的次数
	private Double handlerAvgTime;     //处理的平均时间
	private Double handlerDataAvgSize; //处理请求数据包的平均大小
	
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public Long getCallCount() {
		return callCount;
	}
	public void setCallCount(Long callCount) {
		this.callCount = callCount;
	}
	public Long getFailCount() {
		return failCount;
	}
	public void setFailCount(Long failCount) {
		this.failCount = failCount;
	}
	public Double getHandlerAvgTime() {
		return handlerAvgTime;
	}
	public void setHandlerAvgTime(Double handlerAvgTime) {
		this.handlerAvgTime = handlerAvgTime;
	}
	public Double getHandlerDataAvgSize() {
		return handlerDataAvgSize;
	}
	public void setHandlerDataAvgSize(Double handlerDataAvgSize) {
		this.handlerDataAvgSize = handlerDataAvgSize;
	}
	
	@Override
	public String toString() {
		return "MetricsReporter [serviceName=" + serviceName + ", callCount=" + callCount + ", failCount=" + failCount + ", handlerAvgTime=" + handlerAvgTime
				+ ", handlerDataAvgSize=" + handlerDataAvgSize + "]";
	}
	
}
