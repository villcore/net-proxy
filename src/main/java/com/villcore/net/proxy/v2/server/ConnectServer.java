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

        long time = System.currentTimeMillis();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        LOG.debug("{}", ctx.channel().remoteAddress().toString());
                        LOG.debug("channel read...");
                    }
                });

        LOG.debug("ready to connect to server [{}:{}]...", addr, port);
        Channel[] channel = new Channel[1];
        try {
            time = System.currentTimeMillis();
            channel[0] = bootstrap.connect(addr, port).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(future.isSuccess()) {
                            LOG.debug("{} connect [{}:{}] success ...",channel[0].toString(), addr, port);
                    } else {
                            LOG.debug("{} connect [{}:{}] failed ...",channel[0].toString(), addr, port);
                        }
                    }
                }).channel();
//            channel = bootstrap.connect(addr, port).sync().channel();
//            channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
//                @Override
//                public void operationComplete(Future<? super Void> future) throws Exception {
//                    LOG.debug("remote server channel closed...");
//                }
//            });
            NioSocketChannel dstSocketChannel = (NioSocketChannel) channel[0];
           // LOG.debug("{} connect to server [{}] success ...", System.currentTimeMillis() - time, dstSocketChannel.remoteAddress().toString());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            if(channel[0] != null) {
                channel[0].closeFuture();
            }
        }
    }
}

