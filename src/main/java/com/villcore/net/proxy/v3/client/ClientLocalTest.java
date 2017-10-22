package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.bio.compressor.Compressor;
import com.villcore.net.proxy.bio.compressor.GZipCompressor;
import com.villcore.net.proxy.bio.crypt.CryptHelper;
import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.common.handlers.ChannelClosePackageHandler;
import com.villcore.net.proxy.v3.common.handlers.InvalidDataPackageHandler;
import com.villcore.net.proxy.v3.common.handlers.TransferHandler;
import com.villcore.net.proxy.v3.common.handlers.client.ConnectRespPackageHandler;
import com.villcore.net.proxy.v3.common.handlers.server.connection.ConnectionAuthRespHandler;
import com.villcore.net.proxy.v3.util.ThreadUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class ClientLocalTest {
    private static final Logger LOG = LoggerFactory.getLogger(ClientLocalTest.class);

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException {
        //TODO 配置信息需要从文件中读取
        String proxyPort = "10081";

        String remoteAddress = "127.0.0.1";
        //String remoteAddress = "45.63.120.186";
        String remotePort = "20081";

        String username = "villcore";
        String password = "123123";

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        ScheduleService scheduleService = new ScheduleService();

        //核心的运行任务
        //WriteService
        WriteService writeService = new WriteService(10L);
        writeService.start();
        ThreadUtils.newThread("write-service", writeService, false).start();

        //TunnelManager
        TunnelManager tunnelManager = new TunnelManager(10000);
        tunnelManager.setWriteService(writeService);
        scheduleService.scheduleTaskAtFixedRate(tunnelManager,  5 * 60 * 1000, 5 * 60 * 1000);

        //Connection connection = new Connection();
        ConnectionManager connectionManager = new ConnectionManager(eventLoopGroup, tunnelManager, writeService);
        //Connection connection = connectionManager.connectTo(remoteAddress, Integer.valueOf(remotePort));
        scheduleService.scheduleTaskAtFixedRate(connectionManager, 10 * 60 * 1000, 10 * 60 * 1000);

        //ProcessService
        PackageProcessService packageProcessService = new PackageProcessService(tunnelManager, connectionManager);
        CryptHelper cryptHelper = new CryptHelper();
        Compressor compressor = new GZipCompressor();

        PackageHandler transferDecoderHandler = new TransferHandler(cryptHelper, compressor, true);

        PackageHandler connectAuthRespHandler = new ConnectionAuthRespHandler();
        PackageHandler connectRespHandler = new ConnectRespPackageHandler(tunnelManager);
        PackageHandler channelCloseHandler = new ChannelClosePackageHandler(tunnelManager);
        PackageHandler invalidDataHandler = new InvalidDataPackageHandler(tunnelManager);

        PackageHandler transferEncoderHandler = new TransferHandler(cryptHelper, compressor, false);

        // packageProcessService.addRecvHandler(connectRespHandler, channelCloseHandler /*invalidDataHandler*/);
        //packageProcessService.addRecvHandler(connectRespHandler/*, channelCloseHandler, invalidDataHandler*/);
        packageProcessService.addSendHandler(transferEncoderHandler);
        packageProcessService.addRecvHandler(transferDecoderHandler, connectAuthRespHandler, connectRespHandler, channelCloseHandler, invalidDataHandler);

        packageProcessService.start();
        ThreadUtils.newThread("package-process-service", packageProcessService, false).start();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1 * 60 * 60 * 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 128 * 1024)
                    //.childOption(ChannelOption.AUTO_READ, false)


                    .childHandler(new ClientChildChannelHandlerInitlizer2(tunnelManager, connectionManager, remoteAddress, Integer.valueOf(remotePort), username, password, transferEncoderHandler));
            serverBootstrap.bind(Integer.valueOf(proxyPort)).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
