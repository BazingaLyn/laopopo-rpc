package org.laopopo.example.metrics;

import org.laopopo.client.metrics.Metrics;

import com.codahale.metrics.Counter;

public class CountMetricsTest {
	
	public static void main(String[] args) {
		
		Counter counter = Metrics.counter("test.service");
		
		for(int i = 0;i< 10000;i++){
			counter.inc();
		}
		
		System.out.println(counter.getCount());
		
		counter.dec(counter.getCount());
		
		
		for(int i = 0;i< 10000;i++){
			counter.inc();
		}
		
		System.out.println(counter.getCount());

		
		
	}

}
