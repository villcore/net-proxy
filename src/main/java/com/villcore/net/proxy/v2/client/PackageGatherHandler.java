package com.villcore.net.proxy.v2.client;

import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
//public class PackageGatherHandler extends SimpleChannelInboundHandler<Package> {
//    private static final Logger LOG = LoggerFactory.getLogger(PackageGatherHandler.class);
//
//    public static final String HANDLER_NAME = "pkg-gather";
//    public long count = 0;
//
//    private PackageQeueu packageQeueu;
//    public PackageGatherHandler(PackageQeueu packageQeueu) {
//        this.packageQeueu = packageQeueu;
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, Package msg) throws Exception {
//        LOG.debug("gather packge = {}, total = {}", msg.toString(), ++count);
////        packageQeueu.putPackage(msg);
////        ctx.pipeline().writeAndFlush(Unpooled.EMPTY_BUFFER);
//    }
//}

public class PackageGatherHandler extends ChannelInboundHandlerAdapter{
    private static final Logger LOG = LoggerFactory.getLogger(PackageGatherHandler.class);

    public static final String HANDLER_NAME = "pkg-gather";
    public long count = 0;

    private PackageQeueu sendPackage;
    public PackageGatherHandler(PackageQeueu sendPackage) {
        this.sendPackage = sendPackage;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Package) {
            LOG.debug("gather packge = {}, total = {}", "", ++count);
            Package pkg = (Package) msg;
            sendPackage.putPackage(pkg);
            LOG.debug("put send package queue, queue size = {}", sendPackage.size());
        }
    }
}
