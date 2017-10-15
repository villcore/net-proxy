package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v3.common.Tunnel;
import com.villcore.net.proxy.v3.common.TunnelManager;
import com.villcore.net.proxy.v3.pkg.v2.DefaultDataPackage;
import com.villcore.net.proxy.v3.pkg.v2.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServerTunnelMessageEncoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTunnelMessageEncoder.class);

    private TunnelManager tunnelManager;

    public ServerTunnelMessageEncoder(TunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() > 0) {
            //LOGGER.debug("|||||server tunnel channel read success ...");
            byte[] avaliableBytes = new byte[in.readableBytes()];
            in.readBytes(avaliableBytes);

            Channel channel = ctx.channel();
            Tunnel tunnel = tunnelManager.tunnelFor(channel);
            if (tunnel == null) {
                ctx.channel().close();
                return;
            }
            tunnel.touch(-1);

            int connId = tunnel.getConnId();
            int corrspondConnId = tunnel.getCorrespondConnId();

            if (!tunnel.readWaterMarkerSafe()) {
                tunnel.getChannel().config().setAutoRead(false);
            }

            DefaultDataPackage defaultDataPackage = PackageUtils.buildDataPackage(connId, corrspondConnId, 1L, avaliableBytes);
            //LOG.debug("add send pkg [{}] -> [{}]", connId, corrspondConnId);
            tunnel.addSendPackage(defaultDataPackage);
            //LOGGER.debug("tunnel [{}] -> [{}] need send {} bytes ...", tunnel.getConnId(), tunnel.getCorrespondConnId(), avaliableBytes.length);
            //LOGGER.debug("tunnel [{}] read content ========================\n{}\n==============================", connId, new String(avaliableBytes));
        }
    }
}
