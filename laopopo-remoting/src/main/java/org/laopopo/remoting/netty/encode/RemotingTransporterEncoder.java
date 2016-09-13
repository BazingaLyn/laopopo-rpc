package org.laopopo.remoting.netty.encode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.laopopo.remoting.model.RemotingTransporter;
import static org.laopopo.common.protocal.LaopopoProtocol.MAGIC;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;

/**
 * 
 * @author BazingaLyn
 * @description Netty 对{@link RemotingTransporter}的编码器
 * @time 2016年8月10日
 * @modifytime
 */
@ChannelHandler.Sharable
public class RemotingTransporterEncoder extends MessageToByteEncoder<RemotingTransporter> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RemotingTransporter msg, ByteBuf out) throws Exception {
		doEncodeRemotingTransporter(msg, out);
	}

	private void doEncodeRemotingTransporter(RemotingTransporter msg, ByteBuf out) {
		byte[] body = serializerImpl().writeObject(msg.getCustomHeader());
		
		out.writeShort(MAGIC). 	           //协议头
		writeByte(msg.getTransporterType())// 传输类型 sign 是请求还是响应
		.writeByte(msg.getCode())          // 请求类型requestcode 表明主题信息的类型，也代表请求的类型
		.writeLong(msg.getOpaque())        //requestId
		.writeInt(body.length)             //length
		.writeBytes(body);
	}

}
