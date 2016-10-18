package org.laopopo.example.other;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author BazingaLyn
 * @description 堆copy的测试
 * @time
 * @modifytime
 */
public class StackCopyTest {
	
	private static List<String> ls = new ArrayList<String>();
	
	
	public static void main(String[] args) throws InterruptedException {
		
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				for(int i = 0;i<50000;i++){
					try {
						Thread.sleep(20l);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					ls.add(i+"hehe");
					ls.add("hehe");
					ls.add("hehe");
					ls.add("hehe");
					
				}
			}
		});
		thread.start();
		
		for(int j = 0;j<30;j++){
			callXXmethod();
		}
	}


	private static void callXXmethod() throws InterruptedException {
		List<String> _ls = ls;
		System.out.println("最初的复制的一份的size   "+_ls.size());
		
		Thread.sleep(5000l);
		_ls.remove(1);
		System.out.println("停顿5秒复制的一份的size  "+_ls.size());
		System.out.println("全局变量的一份的size  "+ls.size());
		System.out.println("===========================");
		
	}

}
