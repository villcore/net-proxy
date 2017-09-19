package com.villcore.net.proxy.v3.server;


import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.util.ThreadUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

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
        ScheduleService scheduleService = new ScheduleService();

        //核心的运行任务
        //WriteService
        WriteService writeService = new WriteService();
        writeService.start();
        ThreadUtils.newThread("write-service", writeService, false).start();

        //TunnelManager
        TunnelManager tunnelManager = new TunnelManager();
        tunnelManager.setWriteService(writeService);
        scheduleService.scheduleTaskAtFixedRate(tunnelManager, 1 * 60 * 1000, 1 * 60 * 1000);

        //Connection connection = new Connection();
        ConnectionManager connectionManager = new ConnectionManager(eventLoopGroup, tunnelManager, writeService);
        scheduleService.scheduleTaskAtFixedRate(connectionManager, 10 * 60 * 1000, 10 * 60 * 1000);

        //ProcessService
        PackageProcessService packageProcessService = new PackageProcessService(tunnelManager, connectionManager);
        //authorized handler
        //connect req handler
        //channel close handler
        //data handler
        List<PackageHandler> packageHandlers = new LinkedList<>();

        packageProcessService.start();
        ThreadUtils.newThread("package-process-service", packageProcessService, false).start();

        try {
            LOG.info("start listen [{}] ...", listenPort);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .childHandler(new ServerChildHandlerInitlizer(connectionManager));
            serverBootstrap.bind(Integer.valueOf(listenPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}