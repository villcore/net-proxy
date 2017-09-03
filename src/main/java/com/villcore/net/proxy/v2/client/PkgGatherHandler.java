package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class PkgGatherHandler extends SimpleChannelInboundHandler<Package> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package msg) throws Exception {

    }
}
