package com.villcore.net.proxy.v2.server;

import com.villcore.net.proxy.v2.client.ChildHandlerInitlizer;
import io.netty.bootstrap.ServerBootstrap;
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
        String listenPort = "20080";

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(null);
            serverBootstrap.bind(Integer.valueOf(listenPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
