package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v3.client.ConnectionRecvPackageGatherHandler;
import com.villcore.net.proxy.v3.client.PackageToByteBufOutHandler;
import com.villcore.net.proxy.v3.common.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端 ChildChannel Handler 初始化
 */
public class ServerChildHandlerInitlizer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerChildHandlerInitlizer.class);

    private ConnectionManager connectionManager;
    private TunnelManager tunnelManager;

    public ServerChildHandlerInitlizer(ConnectionManager connectionManager, TunnelManager tunnelManager) {
        this.connectionManager = connectionManager;
        this.tunnelManager = tunnelManager;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LOG.debug("accepot connection from {} ...", ch.remoteAddress().toString());
        Connection connection = connectionManager.acceptConnectTo(ch);

//        ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);

        ch.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()) {
                    connection.setConnected(false);
                }
            }
        });

        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1 * 1024 * 1024, 0, 4, -4, 0));
        ch.pipeline().addLast(new PackageDecoder());
        ch.pipeline().addLast(new ConnIdConvertChannelHandler2());
        ch.pipeline().addLast(new ConnectionRecvPackageGatherHandler(connectionManager));
        ch.pipeline().addLast(new PackageToByteBufOutHandler());
    }
}
