package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.client.Crypt;
import com.villcore.net.proxy.client.Package;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageDecipher extends SimpleChannelInboundHandler<Package> {

    private static final Logger LOG = LoggerFactory.getLogger(PackageDecipher.class);

    private final Crypt crypt;

    // TODO metric.

    public PackageDecipher(Crypt crypt) {
        this.crypt = crypt;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
        byte[] bytes = pkg.getBody();
        byte[] decryptBytes = crypt.decrypt(bytes);

        Package newPkg = Package.buildPackage(new byte[0], decryptBytes);
        // TODO metric.
        LOG.info("Decipher package {} \n {}", Package.toBytes(newPkg).length, new String(decryptBytes));
        if (newPkg != null) {
            ctx.fireChannelRead(newPkg);
        }
    }
}
