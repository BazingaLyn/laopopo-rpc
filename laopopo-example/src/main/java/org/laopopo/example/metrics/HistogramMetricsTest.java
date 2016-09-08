package org.laopopo.example.metrics;

import org.laopopo.client.metrics.Metrics;

import com.codahale.metrics.Histogram;

public class HistogramMetricsTest {
	
	public static void main(String[] args) {
		
		Histogram histogram = Metrics.histogram("test.service");
		
		for(int i = 0;i <100;i++){
			histogram.update((int)(Math.random()*100));
		}
		
		System.out.println(histogram.getSnapshot().size());
		
		for(int i = 0;i <100;i++){
			histogram.update((int)(Math.random()*100));
		}
		
		System.out.println(histogram.getSnapshot().size());
	}

}
