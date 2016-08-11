package org.laopopo.remoting.netty.encode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.laopopo.remoting.model.RemotingTransporter;
import static org.laopopo.common.protocal.LaopopoProtocol.MAGIC;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;

@ChannelHandler.Sharable
public class RemotingTransporterEncoder extends MessageToByteEncoder<RemotingTransporter> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RemotingTransporter msg, ByteBuf out) throws Exception {
		doEncodeRemotingTransporter(msg, out);
	}

	private void doEncodeRemotingTransporter(RemotingTransporter msg, ByteBuf out) {
		byte[] body = serializerImpl().writeObject(msg.getCustomHeader());
		
		out.writeShort(MAGIC).writeByte(msg.getTransporterType()).writeByte(msg.getCode()).writeLong(msg.getOpaque()).writeInt(body.length).writeBytes(body);
	}

}
