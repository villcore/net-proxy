package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.ConnectionManager;
import com.villcore.net.proxy.v3.pkg.v1.Package;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionRecvPackageGatherHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionRecvPackageGatherHandler.class);

    private ConnectionManager connectionManager;

    public ConnectionRecvPackageGatherHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //TODO connection == null ?
        Connection connection = connectionManager.channelFor(ctx.channel());

        if(connection == null) {
            LOG.debug("!!!!!!!!! error, this is a new connection that not managed ...");
            ctx.close();
            return;
        }

        if(msg instanceof Package) {
            Package pkg = Package.class.cast(msg);
            connection.addRecvPackage(pkg);
            connection.connectionTouch(System.currentTimeMillis());
            //LOG.debug("add to recv to conn {}...", connection.toString());
        } else {
            ctx.fireChannelRead(ctx);
        }
    }
}
