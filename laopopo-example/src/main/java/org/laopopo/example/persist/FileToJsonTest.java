package org.laopopo.example.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.utils.PersistUtils;

import com.alibaba.fastjson.JSON;

public class FileToJsonTest {

	private static String storePathRootDir = System.getProperty("user.home") + File.separator + "test" + File.separator + "historyMetrics.json";

	
	public static void main(String[] args) {

		String persistString = PersistUtils.file2String(storePathRootDir);
		
		if(null != persistString){
			@SuppressWarnings("unchecked")
			List<MetricsReporter> metricsReporters = JSON.parseObject(persistString.trim(), List.class);
			for(MetricsReporter metricsReporter : metricsReporters){
				System.out.println(metricsReporter);
			}
//			ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> objects = JSON.parseObject(persistString.trim(), ConcurrentMap.class);
// 
//			for(String service: objects.keySet()){
//				System.out.println(service);
//				ConcurrentMap<Address, MetricsReporter> concurrentMap = objects.get(service);
//				for(Address address : concurrentMap.keySet()){
//					System.out.println(address);
//					System.out.println(concurrentMap.get(address));
//				}
//				
//			}
		}

	}

	public static final String file2String(final String fileName) {
		// 读取txt内容为字符串
		StringBuffer txtContent = new StringBuffer();
		// 每次读取的byte数
		byte[] b = new byte[8 * 1024];
		InputStream in = null;
		try {
			// 文件输入流
			in = new FileInputStream(fileName);
			while (in.read(b) != -1) {
				// 字符串拼接
				txtContent.append(new String(b));
			}
			// 关闭流
			in.close();
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return txtContent.toString();
	}

}
