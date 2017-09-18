package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v2.client.ConnectionManager;
import com.villcore.net.proxy.v2.client.PackageQeueu;
import com.villcore.net.proxy.v2.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v2.pkg.Package;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable

public class PackageGatherHandler extends ChannelInboundHandlerAdapter{
    private static final Logger LOG = LoggerFactory.getLogger(PackageGatherHandler.class);

    public long count = 0;

    private com.villcore.net.proxy.v2.client.ConnectionManager connectionManager;
    private com.villcore.net.proxy.v2.client.PackageQeueu sendPackage;

    public PackageGatherHandler(ConnectionManager connectionManager, PackageQeueu sendPackage) {
        this.connectionManager = connectionManager;
        this.sendPackage = sendPackage;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Package) {
            //LOG.debug("gather packge = {}, total = {}", "", ++count);
            if(msg instanceof DefaultDataPackage) {
                DefaultDataPackage defaultDataPackage = (DefaultDataPackage) msg;
                int localConnId = defaultDataPackage.getLocalConnId();
                NioSocketChannel socketChannel = connectionManager.getChannel(localConnId);
                if(!connectionManager.isChannelConnected(socketChannel)) {
                    connectionManager.pendingPackage(socketChannel, defaultDataPackage);
                    LOG.debug("pending pkg for [{}]...", socketChannel.remoteAddress().toString());
                } else {
                    sendPackage.putPackage(defaultDataPackage);
                }
                return;
            }
            Package pkg = (Package) msg;
            sendPackage.putPackage(pkg);
            //LOG.debug("put write package queue, queue size = {}", sendPackage.size());
        }
    }
}
