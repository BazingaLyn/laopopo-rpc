package org.laopopo.client.metrics;

import java.io.File;
import java.util.concurrent.TimeUnit;

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
 * 性能测量工具
 * @author BazingaLyn
 *
 * @time 2016年6月27日
 */
public class Metrics {

	private static final MetricRegistry metricsRegistry = new MetricRegistry();
	
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

}
