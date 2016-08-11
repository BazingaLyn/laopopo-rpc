package org.laopopo.remoting;

import static org.laopopo.common.protocal.LaopopoProtocol.REQUEST_REMOTING;
import static org.laopopo.common.protocal.LaopopoProtocol.RESPONSE_REMOTING;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.laopopo.common.utils.Pair;
import org.laopopo.remoting.exception.RemotingSendRequestException;
import org.laopopo.remoting.exception.RemotingTimeoutException;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingResponse;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description netty C/S 端的客户端提取，子类去完全netty的一些创建的事情，该抽象类则取完成使用子类创建好的channel去与远程端交互
 *  
 * @time 2016年8月10日10:57:27
 * @modifytime
 */
public class NettyRemotingBase {
	
	private static final Logger logger = LoggerFactory.getLogger(NettyRemotingBase.class);
	
	/******key为请求的opaque value是远程返回的结果封装类******/
	protected final ConcurrentHashMap<Long, RemotingResponse> responseTable = new ConcurrentHashMap<Long, RemotingResponse>(256);
	
	protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;

	public RemotingTransporter invokeSyncImpl(final Channel channel,final RemotingTransporter request,final long timeoutMillis) throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException{
		
		try {
			//构造一个请求的封装体，请求Id和请求结果一一对应
			final RemotingResponse remotingResponse = new RemotingResponse(request.getOpaque(), timeoutMillis, null);
			//将请求放入一个"篮子"中，等远程端填充该篮子中嗷嗷待哺的每一个结果集
			this.responseTable.put(request.getOpaque(), remotingResponse);
			//发送请求
			channel.writeAndFlush(request).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(future.isSuccess()){
						//如果发送对象成功，则设置成功
						remotingResponse.setSendRequestOK(true);
						return;
					}else{
						remotingResponse.setSendRequestOK(false);
					}
					//如果请求发送直接失败，则默认将其从responseTable这个篮子中移除
					responseTable.remove(request.getOpaque());
					//失败的异常信息
					remotingResponse.setCause(future.cause());
					//设置当前请求的返回主体返回体是null(请求失败的情况下，返回的结果肯定是null)
					remotingResponse.putResponse(null);
					logger.warn("use channel [{}] send msg [{}] failed and failed reason is [{}]",channel,request,future.cause().getMessage());
				}
			});
			
			RemotingTransporter remotingTransporter = remotingResponse.waitResponse();
			if(null == remotingTransporter){
				//如果发送是成功的，则说明远程端，处理超时了
				if (remotingResponse.isSendRequestOK()) {
					throw new RemotingTimeoutException(ConnectionUtils.parseChannelRemoteAddr(channel),
	                        timeoutMillis, remotingResponse.getCause());
				}else{
					throw new RemotingSendRequestException(ConnectionUtils.parseChannelRemoteAddr(channel),
							remotingResponse.getCause());
				}
			}
			return remotingTransporter;
		}finally {
			//最后不管怎么样，都需要将其从篮子中移除出来，否则篮子会撑爆的
			this.responseTable.remove(request.getOpaque());
		}
	}
	
	protected void processMessageReceived(ChannelHandlerContext ctx, RemotingTransporter msg) {
		final RemotingTransporter remotingTransporter = msg;
        if (remotingTransporter != null) {
            switch (remotingTransporter.getTransporterType()) {
            case REQUEST_REMOTING:
                processRemotingRequest(ctx, remotingTransporter);
                break;
            case RESPONSE_REMOTING:
                processRemotingResponse(ctx, remotingTransporter);
                break;
            default:
                break;
            }
        }
	}
	
	protected void processRemotingRequest(ChannelHandlerContext ctx, RemotingTransporter remotingTransporter) {
		System.out.println(remotingTransporter);
		remotingTransporter.setTransporterType(RESPONSE_REMOTING);
		ctx.channel().writeAndFlush(remotingTransporter);
	}
	
	protected void processRemotingResponse(ChannelHandlerContext ctx, RemotingTransporter remotingTransporter) {
		System.out.println(remotingTransporter);
		final RemotingResponse remotingResponse = responseTable.get(remotingTransporter.getOpaque());
		if(null != remotingResponse){
			remotingResponse.setRemotingTransporter(remotingTransporter);
			
			responseTable.remove(remotingTransporter.getOpaque());
		}else {
            logger.warn("received response but matched Id is removed from responseTable maybe timeout");
            logger.warn(remotingTransporter.toString());
        }
	}
	
}
