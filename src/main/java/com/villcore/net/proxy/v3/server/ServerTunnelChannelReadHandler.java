package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.v2.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.v2.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server side tunnel channel read handler
 *
 * 将服务端连接到目的地址的ByteBuf打包成 DefaultDataPackage
 */
public class ServerTunnelChannelReadHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ServerTunnelChannelReadHandler.class);

    private TunnelManager tunnelManager;

    public ServerTunnelChannelReadHandler(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        LOG.debug("|||||server tunnel channel read success ...");
        System.out.println("|||||server tunnel channel read success ..., auto read = " +  ctx.channel().config().isAutoRead());

        Channel channel = ctx.channel();

        if(msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;
            Tunnel tunnel = tunnelManager.tunnelFor(channel);
            if(tunnel == null) {
                ctx.channel().close();
                return;
            }
            tunnel.touch(-1);

            int connId = tunnel.getConnId();
            int corrspondConnId = tunnel.getCorrespondConnId();

            if(!tunnel.readWaterMarkerSafe()) {
                tunnel.getChannel().config().setAutoRead(false);
            }
            byte[] bytes = new byte[data.readableBytes()];
            data.readBytes(bytes);
            PackageUtils.release(data);

            DefaultDataPackage defaultDataPackage = PackageUtils.buildDataPackage(connId, corrspondConnId, 1L, bytes);
            //LOG.debug("add send pkg [{}] -> [{}]", connId, corrspondConnId);
            tunnel.addSendPackage(defaultDataPackage);
//            LOG.debug("read content >>>\n [{}]\n>>>>>>>", PackageUtils.toString(data));
            LOG.debug("tunnel [{}] -> [{}] need send {} bytes ...", tunnel.getConnId(), tunnel.getCorrespondConnId(), bytes.length);
            //LOG.debug("tunnel [{}] read content ========================\n{}\n==============================", connId, new String(bytes));

        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(), cause);
    }
}
