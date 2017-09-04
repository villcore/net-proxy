package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class PackageGatherHandler extends SimpleChannelInboundHandler<Package> {

    public static final String HANDLER_NAME = "pkg-gather";
    
    private PackageQeueu packageQeueu;
    public PackageGatherHandler(PackageQeueu packageQeueu) {
        this.packageQeueu = packageQeueu;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package msg) throws Exception {
        packageQeueu.putPackage(msg);
    }
}
