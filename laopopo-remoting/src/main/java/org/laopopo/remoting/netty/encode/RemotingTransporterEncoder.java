package org.laopopo.remoting.netty.encode;

import static org.laopopo.common.protocal.LaopopoProtocol.MAGIC;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

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
	protected void encode(ChannelHandlerContext ctx, RemotingTransporter msg, ByteBuf out) throws IOException   {
		doEncodeRemotingTransporter(msg, out);
	}

	private void doEncodeRemotingTransporter(RemotingTransporter msg, ByteBuf out) throws IOException {
		byte[] body = serializerImpl().writeObject(msg.getCustomHeader());
		
		
		byte isCompress = LaopopoProtocol.UNCOMPRESS;
		if(body.length > 1024){
			isCompress = LaopopoProtocol.COMPRESS;
			body = Snappy.compress(body);
		}
		
		out.writeShort(MAGIC). 	           //协议头
		writeByte(msg.getTransporterType())// 传输类型 sign 是请求还是响应
		.writeByte(msg.getCode())          // 请求类型requestcode 表明主题信息的类型，也代表请求的类型
		.writeLong(msg.getOpaque())        //requestId
		.writeInt(body.length)             //length
		.writeByte(isCompress)			   //是否压缩
		.writeBytes(body);
		
	}

}
