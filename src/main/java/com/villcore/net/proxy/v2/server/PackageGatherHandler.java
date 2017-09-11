package com.villcore.net.proxy.v2.server;

import com.villcore.net.proxy.bio.pkg2.Package;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class PackageGatherHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PackageGatherHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOG.debug("gather package ...{}", msg.getClass().toString());
        if(msg instanceof Package) {
            LOG.debug("server gather a package...");
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
