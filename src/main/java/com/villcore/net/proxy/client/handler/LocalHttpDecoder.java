package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.packet.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LocalHttpDecoder extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(LocalHttpDecoder.class);

    private boolean connected = false;
    private int maxBatchSize;
    private ByteArrayOutputStream requestBatch;

    // TODO metric
    public LocalHttpDecoder(int maxBatchSize) {
        this.requestBatch = new ByteArrayOutputStream();
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (connected) {
            int readableBytesSize = in.readableBytes();
            byte[] newBytes = new byte[readableBytesSize];
            in.readBytes(newBytes, 0, readableBytesSize);
            Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, newBytes);
            out.add(pkg);
            return;
        }

        int readableBytesSize = in.readableBytes();
        if (readableBytesSize < 4) {
            return;
        }
        byte[] newBytes = new byte[readableBytesSize];
        in.readBytes(newBytes, 0, newBytes.length);
        requestBatch.write(newBytes);
        if (isValid(newBytes)) {
            Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, requestBatch.toByteArray());
            out.add(pkg);
            requestBatch = null;
            connected = true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Local read {} bytes, content \n{}", newBytes.length, new String(newBytes, StandardCharsets.UTF_8));
        }
    }

    public boolean isValid(byte[] bytes) {
        int len = bytes.length;
        return requestBatch.size() >= maxBatchSize || (bytes[len - 4] == 13 && bytes[len - 3] == 10 && bytes[len - 2] == 13 && bytes[len - 1] == 10);
    }
}
