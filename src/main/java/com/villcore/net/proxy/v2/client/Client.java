package com.villcore.net.proxy.v2.client;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 *
 */
public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        //load configuration
        //TODO load form conf file
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
        EventLoopGroup wokerLoopGroup = new NioEventLoopGroup();

        String proxyPort = "10081";

//        String remoteAddress = "127.0.0.1";
//        String remotePort = "20081";

        String remoteAddress = "45.63.120.186";
        String remotePort = "20081";

        PackageQeueu sendQueue = new PackageQeueu(1 * 100000);
        PackageQeueu recvQueue = new PackageQeueu(1 * 100000);
        PackageQeueu failQueue = new PackageQeueu(1 * 100000);


        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.start();
        new Thread(connectionManager, "connection-manager").start();

        ClientChannelSendService clientChannelSendService = new ClientChannelSendService(connectionManager, sendQueue, recvQueue, failQueue, eventLoopGroup, remoteAddress, Integer.valueOf(remotePort));
        clientChannelSendService.start();
        new Thread(clientChannelSendService, "client-send-service").start();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup, wokerLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .childHandler(new ChildHandlerInitlizer(connectionManager, sendQueue));
            serverBootstrap.bind(Integer.valueOf(proxyPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
