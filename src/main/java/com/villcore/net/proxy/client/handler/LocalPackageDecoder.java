package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.client.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocalPackageDecoder extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(LocalPackageDecoder.class);

    // TODO metric

    public LocalPackageDecoder() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytesSize = in.readableBytes();
        byte[] newBytes = new byte[readableBytesSize];
        in.readBytes(newBytes, 0, readableBytesSize);
        Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, newBytes);
        out.add(pkg);
        // TODO record bytes.
        LOG.info("Current bytes \n{}", new String(newBytes, StandardCharsets.UTF_8));
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (Exception e) {

        }
    }
}
