package com.villcore.net.proxy.v2.client;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ChildHandlerInitlizer extends ChannelInitializer<SocketChannel> {
    private ConnectionManager connectionManager;
    private PackageQeueu pkgQueue;

    private ChannelInboundHandler packageGatherHandler;

    public ChildHandlerInitlizer(ConnectionManager connectionManager, PackageQeueu pkgQueue) {
        this.connectionManager = connectionManager;
        this.pkgQueue = pkgQueue;
        this.packageGatherHandler = new PackageGatherHandler(this.pkgQueue);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(ProxyDetectHandler.HANDLER_NAME, new ProxyDetectHandler(connectionManager));
        socketChannel.pipeline().addLast(PackageGatherHandler.HANDLER_NAME, this.packageGatherHandler);
    }
}
