package org.laopopo.client.provider.flow.control;


public interface FlowController {
	
	ControlResult flowControl();
	
	void setMaxTimes(int limit);

}
