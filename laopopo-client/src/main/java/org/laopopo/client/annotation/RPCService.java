package org.laopopo.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface RPCService {

	public String serviceName() default "";

	public int weight() default 50;

	public String responsibilityName() default "system";

	public int connCount() default 1;
	
	public boolean isVIPService() default false;
	
	public boolean isSupportDegradeService() default false;
	
	public String degradeServicePath() default "";
	
	public String degradeServiceDesc() default "";
	
	public long maxCallCountInMinute() default 10000;
	
}
