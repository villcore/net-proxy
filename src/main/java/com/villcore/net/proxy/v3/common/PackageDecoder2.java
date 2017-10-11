package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v1.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 读取ByteBuf并根据length信息构建 {@link Package}
 */
public class PackageDecoder2 extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDecoder2.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();
        int totalLen = in.readInt();
        if(in.readableBytes() < totalLen - 4) {
            in.resetReaderIndex();
            return;
        }

        in.resetReaderIndex();

        //in.read
    }
}
