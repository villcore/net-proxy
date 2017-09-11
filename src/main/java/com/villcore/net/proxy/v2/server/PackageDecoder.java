package com.villcore.net.proxy.v2.server;

import com.villcore.net.proxy.v2.pkg.Package;
import com.villcore.net.proxy.v2.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;
import java.util.List;

/**
 * 此类将服务端接收到的 ByteBuf 还原成对应的Package, 该类不可共享
 */
public class PackageDecoder extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(PackageDecoder.class);

    private int packageLen = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(packageLen > 0) {
            if(in.readableBytes() >= packageLen - 4) {
                //complete a package
                ByteBuf byteBuf = in.slice(in.readerIndex(), packageLen - 4);
                in.readerIndex(in.readerIndex() + packageLen - 4);

                Package pkg = Package.valueOf(byteBuf);
                LOG.debug("recv pkg, totalLen = {}, headerLen = {}, bodyLen = {}", pkg.getTotalLen(), pkg.getHeaderLen(), pkg.getBodyLen());
                out.add(pkg);
                packageLen = 0;
            } else {
                //continue;
                return;
            }
        }

        if(in.readableBytes() >= 4) {
            packageLen = in.readInt();
            //System.out.println("pkg len = " + packageLen);
        }
    }
}
