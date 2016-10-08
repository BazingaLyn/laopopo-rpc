package org.laopopo.remoting;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.laopopo.common.rpc.RegisterMeta.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class ConnectionUtils {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);
	
	public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        final SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }
	
	public static Address parseChannelRemoteAddress(final Channel channel) {
         String address = parseChannelRemoteAddr(channel);
         if("".equals(address)){
        	 return null;
         }
         String[] strs = address.split(":");
         return new Address(strs[0], Integer.parseInt(strs[1]));
         
    }

	public static SocketAddress string2SocketAddress(String addr) {
		String[] s = addr.split(":");
        InetSocketAddress isa = new InetSocketAddress(s[0], Integer.valueOf(s[1]));
        return isa;
	}

		public static void closeChannel(Channel channel) {
	        final String addrRemote = parseChannelRemoteAddr(channel);
	        channel.close().addListener(new ChannelFutureListener() {
	            @Override
	            public void operationComplete(ChannelFuture future) throws Exception {
	            	logger.info("closeChannel: close the connection to remote address[{}] result: {}", addrRemote,
	                    future.isSuccess());
	            }
	        });
		
	}

		public static String exceptionSimpleDesc(Exception e) {
			StringBuffer sb = new StringBuffer();
	        if (e != null) {
	            sb.append(e.toString());

	            StackTraceElement[] stackTrace = e.getStackTrace();
	            if (stackTrace != null && stackTrace.length > 0) {
	                StackTraceElement elment = stackTrace[0];
	                sb.append(", ");
	                sb.append(elment.toString());
	            }
	        }

	        return sb.toString();
		}

		public static int getPortFromAddress(String serviceListenAddress) {
			int port = 0;
			if(null != serviceListenAddress){
				String[] strs = serviceListenAddress.split(":");
				if(strs.length == 2){
					port = Integer.parseInt(strs[1]);
				}
			}
			return port;
		}
}
