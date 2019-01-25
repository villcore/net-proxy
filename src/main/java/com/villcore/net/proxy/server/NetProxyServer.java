package com.villcore.net.proxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create by WangTao on 2019/1/25
 */
public class NetProxyServer {

    private static final Logger LOG = LoggerFactory.getLogger(NetProxyServer.class);

    private final int listenPort = 10080;
    private final EventLoopGroup bossEventLoop = new NioEventLoopGroup(1);
    private final EventLoopGroup workerEventLoop = new NioEventLoopGroup();

    public void start() {
        try {
            LOG.info("Server start listen {}...", listenPort);
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossEventLoop, workerEventLoop)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // TODO add child channel.
                            ch.pipeline();
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = serverBootstrap.bind(listenPort).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("Start server error", e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        if (bossEventLoop != null) {
            bossEventLoop.shutdownGracefully();
        }

        if (workerEventLoop != null) {
            workerEventLoop.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        NetProxyServer server = new NetProxyServer();
        server.start();
    }
}
