package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.pkg.ChannelClosePackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * 客户端ChaildChannelHandlerInitlizer
 *
 * 该类主要在建立客户端代理channel后，将channel保存为对应Tunnel对象，加入管理
 */
public class ClientChildChannelHandlerInitlizer extends ChannelInitializer<Channel> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientChildChannelHandlerInitlizer.class);

    private TunnelManager tunnelManager;
    private Connection connection;

    public ClientChildChannelHandlerInitlizer(TunnelManager tunnelManager, Connection connection) {
        this.tunnelManager = tunnelManager;
        this.connection = connection;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {


        Tunnel tunnel = tunnelManager.newTunnel(channel);
        LOG.debug("init tunnel [{}] for channel [{}]...", tunnel.getConnId(), channel.remoteAddress().toString());

        channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()) {
                    tunnel.needClose();
                    ChannelClosePackage channelClosePackage = PackageUtils
                            .buildChannelClosePackage(tunnel.getConnId(), tunnel.getCorrespondConnId(), 1L);
                    connection.addSendPackages(tunnel.drainSendPackages());
                    connection.addSendPackages(Collections.singletonList(channelClosePackage));
                }
            }
        });

        tunnel.setBindConnection(connection);
        tunnelManager.bindConnection(connection, tunnel);
        channel.pipeline().addLast(new ClientTunnelChannelReadHandler(tunnelManager, connection));
        channel.pipeline().addLast(new PackageToByteBufOutHandler());
    }
}
