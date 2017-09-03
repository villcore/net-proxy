package com.villcore.net.proxy.v2.client;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ChildHandlerInitlizer extends ChannelInitializer<SocketChannel> {
    //private ChannelInboundHandler proxyDetectHandler = new ProxyDetectHandler();
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("proxy-detect", new ProxyDetectHandler());
        socketChannel.pipeline().addLast("pkg-gather", proxyDetectHandler);
    }
}
