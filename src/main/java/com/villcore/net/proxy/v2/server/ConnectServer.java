package com.villcore.net.proxy.v2.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectServer {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectServer.class);

    public static void main(String[] args) {
        String addr = "www.speedtest.cn";
        int port = 80;

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        LOG.debug("channel read...");
                    }
                });

        LOG.debug("ready to connect to server [{}:{}]...", addr, port);
        Channel channel = null;
        try {
            channel = bootstrap.connect(addr, port).sync().channel();
            channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    LOG.debug("remote server channel closed...");
                }
            });
            NioSocketChannel dstSocketChannel = (NioSocketChannel) channel;
            LOG.debug("connect to server [{}] success ...", dstSocketChannel.remoteAddress().toString());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            channel.closeFuture();
        }
    }
}

