package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageToByteBufOutHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PackageToByteBufOutHandler.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof Package) {
            Package pkg = (Package) msg;
//            ctx.writeAndFlush(pkg.toByteBuf());
            ctx.write(pkg.toByteBuf());
            return;
        }
//        if(msg instanceof ByteBuf) {
//            ByteBuf byteBuf = (ByteBuf) msg;
//            if(byteBuf.release()) {
//                LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! cur ref cnt = 0 for {}", byteBuf.getClass());
//            } else {
//                ctx.writeAndFlush(byteBuf);
//            }
//            return;
//        }
//        if(msg instanceof ByteBuf) {
//            ByteBuf byteBuf = (ByteBuf) msg;
//            byteBuf.release();
//        }
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! cur ref cnt = 0 for {}", msg.getClass());
        ctx.writeAndFlush(msg);
//        ctx.write(msg);

    }
}
