package org.laopopo.client.provider.model;

import static org.laopopo.common.utils.Constants.DEFAULT_CONNECTION_COUNT;
import static org.laopopo.common.utils.Constants.DEFAULT_WEIGHT;

import java.util.List;

import org.laopopo.client.provider.flow.control.FlowController;

/**
 * 
 * @author BazingaLyn
 * @description provider端需要编织的service类
 * @time
 * @modifytime
 */
public class ServiceWrapper {

	/**** 原生类 ****/
	private Object serviceProvider;

	/**** 降级类  默认入参和方法名与原生类一样******/
	private Object mockDegradeServiceProvider;

	/****** 服务名 *****/
	private String serviceName;

	/****** 该系统的负责人 *******/
	private String responsiblityName;

	/******* 该类中的方法名 *******/
	private String methodName;

	/****** 该方法的入参 ******/
	private List<Class<?>[]> paramters;

	/******该方法是否可以降级********/
	private boolean isSupportDegradeService;
	
	/*****降级方法的路径*******/
	private String degradeServicePath;
	
	/****降级方法的描述****/
	private String degradeServiceDesc;
	
	private boolean isVIPService;

	private volatile int weight = DEFAULT_WEIGHT;

	private volatile int connCount = DEFAULT_CONNECTION_COUNT;

	private volatile FlowController flowController;

	public ServiceWrapper(Object serviceProvider, Object mockDegradeServiceProvider, String serviceName,
			String responsiblityName, String methodName, List<Class<?>[]> paramters, boolean isSupportDegradeService, String degradeServicePath,
			String degradeServiceDesc, int weight, int connCount,boolean isVIPService, FlowController flowController) {
		this.serviceProvider = serviceProvider;
		this.mockDegradeServiceProvider = mockDegradeServiceProvider;
		this.serviceName = serviceName;
		this.responsiblityName = responsiblityName;
		this.methodName = methodName;
		this.paramters = paramters;
		this.isSupportDegradeService = isSupportDegradeService;
		this.degradeServicePath = degradeServicePath;
		this.degradeServiceDesc = degradeServiceDesc;
		this.weight = weight;
		this.connCount = connCount;
		this.isVIPService = isVIPService;
		this.flowController = flowController;
	}

	public Object getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(Object serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public Object getMockDegradeServiceProvider() {
		return mockDegradeServiceProvider;
	}

	public void setMockDegradeServiceProvider(Object mockDegradeServiceProvider) {
		this.mockDegradeServiceProvider = mockDegradeServiceProvider;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getResponsiblityName() {
		return responsiblityName;
	}

	public void setResponsiblityName(String responsiblityName) {
		this.responsiblityName = responsiblityName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public List<Class<?>[]> getParamters() {
		return paramters;
	}

	public void setParamters(List<Class<?>[]> paramters) {
		this.paramters = paramters;
	}

	public boolean isSupportDegradeService() {
		return isSupportDegradeService;
	}

	public void setSupportDegradeService(boolean isSupportDegradeService) {
		this.isSupportDegradeService = isSupportDegradeService;
	}

	public String getDegradeServicePath() {
		return degradeServicePath;
	}

	public void setDegradeServicePath(String degradeServicePath) {
		this.degradeServicePath = degradeServicePath;
	}

	public String getDegradeServiceDesc() {
		return degradeServiceDesc;
	}

	public void setDegradeServiceDesc(String degradeServiceDesc) {
		this.degradeServiceDesc = degradeServiceDesc;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getConnCount() {
		return connCount;
	}

	public void setConnCount(int connCount) {
		this.connCount = connCount;
	}

	public FlowController getFlowController() {
		return flowController;
	}

	public void setFlowController(FlowController flowController) {
		this.flowController = flowController;
	}
	
	public boolean isVIPService() {
		return isVIPService;
	}

	public void setVIPService(boolean isVIPService) {
		this.isVIPService = isVIPService;
	}
	

	@Override
	public String toString() {
		return "ServiceWrapper [serviceProvider=" + serviceProvider + ", mockDegradeServiceProvider=" + mockDegradeServiceProvider + ", serviceName="
				+ serviceName + ", responsiblityName=" + responsiblityName + ", methodName=" + methodName + ", paramters=" + paramters
				+ ", isSupportDegradeService=" + isSupportDegradeService + ", degradeServicePath=" + degradeServicePath + ", degradeServiceDesc="
				+ degradeServiceDesc + ", isVIPService=" + isVIPService + ", weight=" + weight + ", connCount=" + connCount + ", flowController="
				+ flowController + "]";
	}
	
}
