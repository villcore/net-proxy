package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.common.*;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOG.debug("init channel [{}]...", channel.remoteAddress().toString());

        Tunnel tunnel = tunnelManager.newTunnel(channel);
        tunnelManager.bindConnection(connection, tunnel);
        //channel add handler
        //TunnelReadHandler 解析proxy，打包Package，这里需要对不再读取的三种情形做判断
        channel.pipeline().addLast(new TunnelReadHandler(tunnelManager));
        channel.pipeline().addLast(new PackageToByteBufOutHandler());
    }

    private static class TestReader extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println(PackageUtils.toString(byteBuf));
        }
    };
}
