package com.villcore.net.proxy.v2.client;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import com.villcore.net.proxy.v2.pkg.Package;
import com.villcore.net.proxy.v2.pkg.PackageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBufToPackageHandler extends ChannelInboundHandlerAdapter{
    private static final Logger LOG = LoggerFactory.getLogger(ByteBufToPackageHandler.class);

    public static final String HANDLER_NAME = "bytebuf-pkg";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int bodyLen = byteBuf.writerIndex() - byteBuf.readerIndex();
            int headerLen = 0;
            int totalLen = Package.FIXED_LEN + headerLen + bodyLen;

            Package pkg = new Package();
            pkg.setBody(byteBuf);
            pkg.setHeader(Unpooled.EMPTY_BUFFER);
            pkg.setPkgType(PackageType.PKG_BASIC);

            ctx.writeAndFlush(pkg);
        }
    }
}
