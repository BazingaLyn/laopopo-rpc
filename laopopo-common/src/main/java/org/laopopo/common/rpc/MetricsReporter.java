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
	
	private String host;                     //host	
	private int port; 				         //端口号
	private String serviceName;              //统计的服务名
	private Long callCount = 0l;             //调用的次数
	private Long failCount = 0l;             //失败的次数
	private Long requestSize = 0l;           //请求的大小
	private Long totalReuqestTime = 0l;      //总请求的时间
	
	
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
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Long getRequestSize() {
		return requestSize;
	}
	public void setRequestSize(Long requestSize) {
		this.requestSize = requestSize;
	}
	public Long getTotalReuqestTime() {
		return totalReuqestTime;
	}
	public void setTotalReuqestTime(Long totalReuqestTime) {
		this.totalReuqestTime = totalReuqestTime;
	}
	@Override
	public String toString() {
		return "MetricsReporter [host=" + host + ", port=" + port + ", serviceName=" + serviceName + ", callCount=" + callCount + ", failCount=" + failCount
				+ ", requestSize=" + requestSize + ", totalReuqestTime=" + totalReuqestTime + "]";
	}
	
}
