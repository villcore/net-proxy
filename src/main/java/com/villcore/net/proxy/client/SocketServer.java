package com.villcore.net.proxy.client;

import com.villcore.net.proxy.client.handler.ClientChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(SocketServer.class);

    private final int listenPort;
    private final String remoteAddress;
    private final int remotePort;
    private final String password;

    private EventLoopGroup bossEventGroup;
    private EventLoopGroup workerEventGroup;

    public SocketServer(int listenPort, String remoteAddress, int remotePort, String password) {
        this.listenPort = listenPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.password = password;
    }

    public void startup() {
        LOG.info("Starting SocketServer");
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            bossEventGroup = new NioEventLoopGroup(1);
            workerEventGroup = new NioEventLoopGroup(2);             // defulat processor * 2

            serverBootstrap.group(bossEventGroup, workerEventGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false))
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_RCVBUF, 1024 * 1024 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024 * 1024)
                    .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false))
                    .childHandler(new ClientChannelInitializer(remoteAddress, remotePort, password));

            ChannelFuture channelFuture = serverBootstrap.bind(listenPort).sync()
                    .addListener(future -> LOG.info("Start SocketServer completed"));
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("Start SocketServer error", e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        shutdownEventGroup(this.bossEventGroup);
        shutdownEventGroup(this.workerEventGroup);
    }

    private void shutdownEventGroup(EventLoopGroup eventLoopGroup) {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
