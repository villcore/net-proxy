package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.crypt.Crypt;
import com.villcore.net.proxy.packet.Package;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageDecipher extends SimpleChannelInboundHandler<Package> {

    private static final Logger LOG = LoggerFactory.getLogger(PackageDecipher.class);

    // TODO metric.
    private Crypt crypt;

    public PackageDecipher() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
        boolean localForward = ctx.channel().attr(AttributeKey.<Boolean>valueOf(ChannelAttrKeys.LOCAL_FORWARD)).get();
        if (localForward) {
            ctx.fireChannelRead(pkg);
            return;
        }

        if (crypt == null) {
            Attribute<Crypt> cryptAttribute = ctx.channel().attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT));
            crypt = cryptAttribute.get();
        }
        byte[] bytes = pkg.getBody();
        byte[] decryptBytes = crypt.decrypt(bytes);

        Package newPkg = Package.buildPackage(new byte[0], decryptBytes);
        // TODO metric
        if (LOG.isDebugEnabled()) {
            LOG.debug("Decipher package body {} bytes, content \n {}", bytes.length, new String(decryptBytes));
        }
        if (newPkg != null) {
            ctx.fireChannelRead(newPkg);
        }
    }
}
