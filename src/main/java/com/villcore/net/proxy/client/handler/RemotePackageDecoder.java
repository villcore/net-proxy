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

public class RemotePackageDecoder extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(RemotePackageDecoder.class);

    // TODO metric
    private int packageSize;
    private int headerLen;
    private int bodyLen;
    private boolean headerRead;

    public RemotePackageDecoder() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!headerRead && in.readableBytes() >= 12) {
            packageSize = in.readInt();
            headerLen = in.readInt();
            bodyLen = in.readInt();
            headerRead = true;
            LOG.info("Decode remote package size {}, header len {}, body len {}", packageSize, headerLen, bodyLen);
        } else {
            return;
        }

        if (in.readableBytes() >= headerLen + bodyLen) {
            byte[] header = new byte[headerLen];
            byte[] body = new byte[bodyLen];
            in.readBytes(header).readBytes(body);

            Package pkg = Package.buildPackage(header, body);
            out.add(pkg);

            packageSize = 0;
            headerLen = 0;
            bodyLen = 0;
            headerRead = false;
        }
    }
}
