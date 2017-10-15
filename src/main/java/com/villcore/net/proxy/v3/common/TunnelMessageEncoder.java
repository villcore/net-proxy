package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v2.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelMessageEncoder extends MessageToByteEncoder<Package> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TunnelMessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Package msg, ByteBuf out) throws Exception {
        if(msg != null) {
            byte[] body = msg.getBody();
            out.writeBytes(body);
//            LOGGER.debug("write to tunnel {} bytes ...", body.length);
//            LOGGER.debug("write to tunnel content = \n{}\n", new String(body));
        } else {
            ctx.writeAndFlush(msg);
        }
    }
}
