package org.laopopo.client.provider;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.remoting.model.RemotingTransporter;


/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class RegistryController {
	
	private DefaultProvider defaultProvider;

	public RegistryController(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	public void publishedAndStartProvider(String address) throws InterruptedException, RemotingException {
		
		List<RemotingTransporter> transporters = defaultProvider.getPublishRemotingTransporters();
		
		String[] addresses = address.split(",");
		if(null != addresses && addresses.length > 0 && null != transporters && !transporters.isEmpty()){
			for(String eachAddress : addresses){
				for(RemotingTransporter request : transporters){
					RemotingTransporter remotingTransporter = defaultProvider.getNettyRemotingClient().invokeSync(eachAddress, request, 3000000);
				}
			}
		}
	}


}
