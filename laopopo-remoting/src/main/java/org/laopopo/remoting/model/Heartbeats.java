package org.laopopo.remoting.model;

import static org.laopopo.common.protocal.LaopopoProtocol.HEAD_LENGTH;
import static org.laopopo.common.protocal.LaopopoProtocol.HEARTBEAT;
import static org.laopopo.common.protocal.LaopopoProtocol.MAGIC;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
@SuppressWarnings("deprecation")
public class Heartbeats {

    private static final ByteBuf HEARTBEAT_BUF;
    
    static {
        ByteBuf buf = Unpooled.buffer(HEAD_LENGTH);
        buf.writeShort(MAGIC);
        buf.writeByte(HEARTBEAT);
        buf.writeByte(0);
        buf.writeLong(0);
        buf.writeInt(0);
        HEARTBEAT_BUF = Unpooled.unmodifiableBuffer(Unpooled.unreleasableBuffer(buf));
    }

    /**
     * Returns the shared heartbeat content.
     */
    public static ByteBuf heartbeatContent() {
        return HEARTBEAT_BUF.duplicate();
    }
}
