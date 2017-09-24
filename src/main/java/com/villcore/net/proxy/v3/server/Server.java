package com.villcore.net.proxy.v3.server;


import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.common.handlers.ChannelClosePackageHandler;
import com.villcore.net.proxy.v3.common.handlers.InvalidDataPackageHandler;
import com.villcore.net.proxy.v3.common.handlers.client.ConnectRespPackageHandler;
import com.villcore.net.proxy.v3.common.handlers.server.ConnectReqPackageHandler;
import com.villcore.net.proxy.v3.util.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
        ScheduleService scheduleService = new ScheduleService();
        Bootstrap bootstrap = new Bootstrap();

        //核心的运行任务
        //WriteService
        WriteService writeService = new WriteService(50L);
        writeService.start();
        ThreadUtils.newThread("write-service", writeService, false).start();

        //TunnelManager
        TunnelManager tunnelManager = new TunnelManager(20000);
        tunnelManager.setWriteService(writeService);
        scheduleService.scheduleTaskAtFixedRate(tunnelManager, 300 * 1000, 300 * 1000);

        //Connection connection = new Connection();
        ConnectionManager connectionManager = new ConnectionManager(eventLoopGroup, tunnelManager, writeService);
        scheduleService.scheduleTaskAtFixedRate(connectionManager, 10 * 60 * 1000, 10 * 60 * 1000);

        //ProcessService
        PackageProcessService packageProcessService = new PackageProcessService(tunnelManager, connectionManager);
        //authorized handler
        //connect req handler //channel close handler  // invalid data handler
        PackageHandler connectReqHandler = new ConnectReqPackageHandler(eventLoopGroup, writeService, tunnelManager, connectionManager);
        PackageHandler channelCloseHandler = new ChannelClosePackageHandler(tunnelManager);
        PackageHandler invalidDataHandler = new InvalidDataPackageHandler(tunnelManager);

        //packageProcessService.addRecvHandler(connectReqHandler, channelCloseHandler /*invalidDataHandler*/);

        //packageProcessService.addRecvHandler(connectReqHandler /*, channelCloseHandler, invalidDataHandler*/);
        packageProcessService.addRecvHandler(connectReqHandler, channelCloseHandler, invalidDataHandler);

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
                    .childHandler(new ServerChildHandlerInitlizer(connectionManager, tunnelManager));
            serverBootstrap.bind(Integer.valueOf(listenPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
