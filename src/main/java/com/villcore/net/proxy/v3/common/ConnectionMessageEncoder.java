package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v2.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class ConnectionMessageEncoder extends MessageToByteEncoder<Package> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionMessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Package msg, ByteBuf out) throws Exception {
        if(msg != null) {
            byte[] fixed = msg.getFixed();
            byte[] header = msg.getHeader();
            byte[] body = msg.getBody();
            out.writeBytes(fixed);
            out.writeBytes(header);
            out.writeBytes(body);
//            ctx.flush();
//            LOGGER.debug("write total = {} ,fix = {}, header = {}, body = {} byte to channel for connection ...", new Object[]{
//                    ByteBuffer.wrap(fixed).getInt(), fixed.length, header.length, body.length
//            });
        } else {
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        }
    }
}
