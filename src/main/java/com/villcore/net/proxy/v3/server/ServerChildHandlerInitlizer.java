package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v3.client.ClientPackageDecoder;
import com.villcore.net.proxy.v3.client.ConnectionRecvPackageGatherHandler;
import com.villcore.net.proxy.v3.client.PackageToByteBufOutHandler;
import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.ConnectionManager;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端 ChildChannel Handler 初始化
 */
public class ServerChildHandlerInitlizer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerChildHandlerInitlizer.class);

    private ConnectionManager connectionManager;

    public ServerChildHandlerInitlizer(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        connectionManager.acceptConnectTo(ch);
        ch.pipeline().addLast(new ClientPackageDecoder());
        ch.pipeline().addLast(new ConnectionRecvPackageGatherHandler(connectionManager));
        ch.pipeline().addLast(new PackageToByteBufOutHandler());
    }
}
