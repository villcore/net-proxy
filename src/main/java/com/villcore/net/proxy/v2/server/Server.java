package com.villcore.net.proxy.v2.server;

import com.villcore.net.proxy.v2.client.ChildHandlerInitlizer;
import com.villcore.net.proxy.v2.client.ClientChannelSendService;
import com.villcore.net.proxy.v2.client.ConnectionManager;
import com.villcore.net.proxy.v2.client.PackageQeueu;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * remote vps server side
 */
public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        //load configuration
        //TODO load form conf file
        String listenPort = "20081";
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

        PackageQeueu sendQueue = new PackageQeueu(1 * 100000);
        PackageQeueu recvQueue = new PackageQeueu(1 * 100000);
        PackageQeueu failQueue = new PackageQeueu(1 * 100000);

        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.start();
        new Thread(connectionManager, "connection-manager").start();

        ServerChannelSendService serverChannelSendService = new ServerChannelSendService(connectionManager, sendQueue, recvQueue, failQueue, eventLoopGroup);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerChildHandlerInitlizer(recvQueue, connectionManager, serverChannelSendService));
            serverBootstrap.bind(Integer.valueOf(listenPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
