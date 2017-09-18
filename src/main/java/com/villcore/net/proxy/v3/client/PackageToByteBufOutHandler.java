package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class PackageToByteBufOutHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof Package) {
            Package pkg = (Package) msg;
            ctx.writeAndFlush(pkg.toByteBuf());
        }
        ctx.writeAndFlush(msg);
    }
}
