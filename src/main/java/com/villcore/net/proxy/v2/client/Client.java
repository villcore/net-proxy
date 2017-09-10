package com.villcore.net.proxy.v2.client;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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

        String proxyPort = "10081";

        String remoteAddress = "127.0.0.1";
        String remotePort = "20080";

        //
        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.start();
        new Thread(connectionManager, "connection-manager").start();

        PackageQeueu pkgQueue = new PackageQeueu(1 * 1000);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //EventLoopGroup workerGroup = new NioEventLoopGroup();

        NioSocketChannel remoteChannel = null;
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    //.handler(new RemoteChannelInitlizer(remoteChannel, bossGroup, new InetSocketAddress(remoteAddress, Integer.valueOf(remotePort))))
                    .childHandler(new ChildHandlerInitlizer(connectionManager, pkgQueue));
            serverBootstrap.bind(Integer.valueOf(proxyPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            bossGroup.shutdownGracefully();
            //workerGroup.shutdownGracefully();
        }
    }
}
