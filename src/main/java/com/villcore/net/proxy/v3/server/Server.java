package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v2.client.ConnectionManager;
import com.villcore.net.proxy.v2.client.PackageQeueu;
import com.villcore.net.proxy.v2.server.ServerChannelSendService;
import com.villcore.net.proxy.v2.server.ServerChildHandlerInitlizer;
import com.villcore.net.proxy.v3.client.ClientChildChannelHandlerInitlizer;
import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.PackageProcessService;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.common.WriteService;
import com.villcore.net.proxy.v3.util.ThreadUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


        //核心的运行任务
        //Connection, tunnelManager
        Connection connection = new Connection();

        //TunnelManager
        TunnelManager tunnelManager = new TunnelManager();

        //ProcessService
        PackageProcessService packageProcessService = new PackageProcessService(tunnelManager);
        packageProcessService.start();
        ThreadUtils.newThread("package-process-service", packageProcessService, false).start();

        //WriteService
        WriteService writeService = new WriteService();
        writeService.start();
        ThreadUtils.newThread("write-service", writeService, false).start();

        tunnelManager.setWriteService(writeService);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .childHandler(new ClientChildChannelHandlerInitlizer(eventLoopGroup, tunnelManager, packageProcessService, writeService));
            serverBootstrap.bind(Integer.valueOf(listenPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
