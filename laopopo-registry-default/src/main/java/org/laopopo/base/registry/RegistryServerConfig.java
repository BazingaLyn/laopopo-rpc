package org.laopopo.base.registry;

import java.io.File;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心的一些基本配置文件
 * @time 2016年9月8日
 * @modifytime
 */
public class RegistryServerConfig {
	
	//持久化保存的位置
	private String storePathRootDir = System.getProperty("user.home") + File.separator + "test" + File.separator + "serviceInfo.json";
	
	//每个多久时间刷盘到硬盘上，默认30s
	private int persistTime = 30;

	public String getStorePathRootDir() {
		return storePathRootDir;
	}

	public void setStorePathRootDir(String storePathRootDir) {
		this.storePathRootDir = storePathRootDir;
	}

	public int getPersistTime() {
		return persistTime;
	}

	public void setPersistTime(int persistTime) {
		this.persistTime = persistTime;
	}
	
	

}
