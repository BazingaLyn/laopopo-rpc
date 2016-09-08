package org.laopopo.example.persist;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.utils.PersistUtils;

import com.alibaba.fastjson.JSON;

public class FastjsonTest {
	
	
	private static String storePathRootDir = System.getProperty("user.home") + File.separator + "test" + File.separator + "historyMetrics.json";
	public static void main(String[] args) throws IOException {
		
		
		String existStr = PersistUtils.file2String(storePathRootDir);
		
		List<MetricsReporter> metricsReporters = JSON.parseArray(existStr.trim(), MetricsReporter.class);
		
		for(MetricsReporter s :metricsReporters){
			System.out.println(s);
		}
		
//		
//		
//		
//		
//		
//		Student student1 = new Student(1, "王诩文", 26);
//		Student student2 = new Student(2, "朴俊", 27);
//		Student student3 = new Student(3, "陆维梁", 28);
//		Student student4 = new Student(4, "曾卓", 28);
//		List<Student> students = new ArrayList<Student>();
//		students.add(student1);
//		students.add(student2);
//		students.add(student3);
//		students.add(student4);
//		//json to str
//		String str = JSON.toJSONString(students);
//		
//		//获取到自定义的用户名
//		String fileName = storePathRootDir;
//		
//		PersistUtils.string2File(str, fileName);
//		
//		String existStr = PersistUtils.file2String(fileName);
//		
//		List<Student> recoverStudents =JSON.parseArray(existStr.trim(), Student.class);
//		
//		System.out.println(recoverStudents);
//		
//			for(Student s :recoverStudents){
//				System.out.println(s);
//			}
	}

}
