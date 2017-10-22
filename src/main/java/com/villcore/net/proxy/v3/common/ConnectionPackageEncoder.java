package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v2.Package;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPackageEncoder extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPackageEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Package) {
            Package pkg = (Package) msg;
            LOGGER.debug("send pkg type = {}", pkg.getPkgType());
            if (msg != null) {
                byte[] fixed = pkg.getFixed();
                byte[] header = pkg.getHeader();
                byte[] body = pkg.getBody();

                ctx.write(Unpooled.wrappedBuffer(fixed, header, body));
//            ctx.flush();
//            LOGGER.debug("write total = {} ,fix = {}, header = {}, body = {} byte to channel for connection ...", new Object[]{
//                    ByteBuffer.wrap(fixed).getInt(), fixed.length, header.length, body.length
//            });
            } else {
                ctx.pipeline().write(msg);
            }
        }
    }
}
