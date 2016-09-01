package org.laopopo.client.metrics;

import java.io.File;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.laopopo.common.rpc.MetricsReporter;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class Metrics {

	private static final MetricRegistry metricsRegistry = new MetricRegistry();
	
	private static ConcurrentMap<String, MetricsReporter> globalMetricsReporter = new ConcurrentHashMap<String, MetricsReporter>();
	
	private static final ScheduledReporter scheduledReporter;
	
	private static final boolean isPaperReport = false;
	static {
		
		if(isPaperReport){
			
			scheduledReporter = CsvReporter.forRegistry(metricsRegistry).build(new File("C://metrics"));
			
		}else{
			 ScheduledReporter _reporter;
	            try {
	                _reporter = Slf4jReporter.forRegistry(metricsRegistry)
	                                            .withLoggingLevel(Slf4jReporter.LoggingLevel.WARN)
	                                            .build();
	            } catch (NoClassDefFoundError e) {
	                // No Slf4j
	                _reporter = ConsoleReporter.forRegistry(metricsRegistry).build();
	            }
	            scheduledReporter = _reporter;
		}
		
		scheduledReporter.start(1, TimeUnit.MINUTES);
	}
	
	public static void scheduledSendReport(){
		
		SortedMap<String, Meter> map = metricsRegistry.getMeters();                //失败调用的统计
		SortedMap<String, Histogram> histograms = metricsRegistry.getHistograms(); //统计请求体的大小
		SortedMap<String, Timer> timer = metricsRegistry.getTimers();              //请求时间的统计||请求的统计
		
		if(null != map && map.keySet() != null && map.keySet().size() > 0){
			//循环每一个统计的serviceName的信息
			for(String serviceName :map.keySet()){
				
				MetricsReporter metricsReporter = globalMetricsReporter.get(serviceName);
				if(null == metricsReporter){
					metricsReporter = new MetricsReporter();
					metricsReporter.setServiceName(serviceName);
				}
				Meter meter = map.get(serviceName+"::rejection");
				metricsReporter.setFailCount(meter.getCount()); //设置失败次数
			}
		}
		
		if(null != histograms && histograms.keySet() != null && histograms.keySet().size() > 0){
			//循环每一个统计的serviceName的信息
			for(String serviceName :map.keySet()){
				
				MetricsReporter metricsReporter = globalMetricsReporter.get(serviceName);
				if(null == metricsReporter){
					metricsReporter = new MetricsReporter();
					metricsReporter.setServiceName(serviceName);
				}
				Histogram Histogram = histograms.get(serviceName+"::requestSize");
				metricsReporter.setHandlerDataAvgSize(Histogram.getSnapshot().getMean()); //设置请求体的平均大小
			}
		}
		
		if(null != timer && timer.keySet() != null && timer.keySet().size() > 0){
			//循环每一个统计的serviceName的信息
			for(String serviceName :map.keySet()){
				
				MetricsReporter metricsReporter = globalMetricsReporter.get(serviceName);
				if(null == metricsReporter){
					metricsReporter = new MetricsReporter();
					metricsReporter.setServiceName(serviceName);
				}
				Timer currentTime = timer.get(serviceName+"::processing");
				metricsReporter.setCallCount(currentTime.getCount()); //设置请求的次数
				metricsReporter.setHandlerAvgTime(currentTime.getSnapshot().getMean());
			}
		}
	}
	
	
	private Metrics() {}
	
    public static MetricRegistry metricsRegistry() {
        return metricsRegistry;
    }

    public static Meter meter(String name) {
        return metricsRegistry.meter(name);
    }

    public static Meter meter(Class<?> clazz, String... names) {
        return metricsRegistry.meter(MetricRegistry.name(clazz, names));
    }

    public static Timer timer(String name) {
        return metricsRegistry.timer(name);
    }

    public static Timer timer(Class<?> clazz, String... names) {
        return metricsRegistry.timer(MetricRegistry.name(clazz, names));
    }

    public static Counter counter(String name) {
        return metricsRegistry.counter(name);
    }

    public static Counter counter(Class<?> clazz, String... names) {
        return metricsRegistry.counter(MetricRegistry.name(clazz, names));
    }

    public static Histogram histogram(String name) {
        return metricsRegistry.histogram(name);
    }

    public static Histogram histogram(Class<?> clazz, String... names) {
        return metricsRegistry.histogram(MetricRegistry.name(clazz, names));
    }


	public static ConcurrentMap<String, MetricsReporter> getGlobalMetricsReporter() {
		return globalMetricsReporter;
	}

	public static void setGlobalMetricsReporter(ConcurrentMap<String, MetricsReporter> globalMetricsReporter) {
		Metrics.globalMetricsReporter = globalMetricsReporter;
	}
    

}
