package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * server side tunnel channel read handler
 *
 * 将服务端连接到目的地址的ByteBuf打包成 DefaultDataPackage
 */
public class ServerTunnelChannelReadHandler extends ChannelInboundHandlerAdapter {
    private TunnelManager tunnelManager;

    public ServerTunnelChannelReadHandler(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if(msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;
            Tunnel tunnel = tunnelManager.tunnelFor(channel);
            tunnel.touch(null);
            int connId = tunnel.getConnId();
            int corrspondConnId = tunnel.getCorrespondConnId();

            DefaultDataPackage defaultDataPackage = PackageUtils.buildDataPackage(corrspondConnId, connId, 1L, data);
//            ctx.fireChannelRead(defaultDataPackage);
            tunnel.addSendPackage(defaultDataPackage);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
