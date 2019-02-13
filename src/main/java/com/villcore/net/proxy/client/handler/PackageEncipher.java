package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.client.Crypt;
import com.villcore.net.proxy.client.Package;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class PackageEncipher extends SimpleChannelInboundHandler<Package> {

    private static final Logger LOG = LoggerFactory.getLogger(PackageEncipher.class);

    private Crypt crypt;
    private boolean ivSend;

    // TODO metric.

    public PackageEncipher(Crypt crypt) {
        this.crypt = crypt;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
        Package newPkg = null;
        byte[] bytes = pkg.getBody();
        if(!ivSend) {
            byte[] iv = crypt.getIv();
            byte[] encryptHeader;
            byte[] encryptBody = crypt.encrypt(bytes);

            ByteBuffer tmp = ByteBuffer.wrap(new byte[4 + iv.length]);
            tmp.putInt(iv.length);
            tmp.put(iv);
            encryptHeader = tmp.array();

            newPkg = Package.buildPackage(encryptHeader, encryptBody);
            ivSend = true;
        } else {
            byte[] encryptHeader = new byte[0];
            byte[] encryptBody = crypt.encrypt(bytes);
            newPkg = Package.buildPackage(encryptHeader, encryptBody);
        }

        // TODO metric.
        LOG.info("Encipher package {} \n {}", Package.toBytes(newPkg).length, new String(crypt.decrypt(newPkg.getBody())));
        if (newPkg != null) {
            ctx.fireChannelRead(newPkg);
        }
    }
}
