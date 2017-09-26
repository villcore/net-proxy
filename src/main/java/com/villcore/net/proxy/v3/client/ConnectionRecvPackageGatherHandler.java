package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.common.Connection;
import com.villcore.net.proxy.v3.common.ConnectionManager;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

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
        }
        if(msg instanceof Package) {
            Package pkg = Package.class.cast(msg);
            //LOG.debug("get pkg = {}", PackageUtils.toString(pkg.toByteBuf().copy()));
            Package correctpkg = pkg;
            connection.addRecvPackages(Collections.singletonList(correctpkg));
            connection.connectionTouch(System.currentTimeMillis());
        } else {
            ctx.fireChannelRead(ctx);
        }
    }
}
