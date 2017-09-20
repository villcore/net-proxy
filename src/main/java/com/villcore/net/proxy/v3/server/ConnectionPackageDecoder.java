package com.villcore.net.proxy.v3.server;

import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 此类将服务端接收到的 ByteBuf 还原成对应的Package, 该类不可共享
 */
public class ConnectionPackageDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPackageDecoder.class);

    private int packageLen = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOG.debug("connection package decoder , msg = {}, {}", msg.getClass().toString(), !(msg instanceof ByteBuf));
        if(!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf in = (ByteBuf) msg;

        if (packageLen > 0) {
            if (in.readableBytes() >= packageLen - 4) {
                LOG.debug("package bytebuf to package ...");
                ByteBuf byteBuf = in.slice(in.readerIndex(), packageLen - 4);
                in.readerIndex(in.readerIndex() + packageLen - 4);
                Package pkg = Package.valueOf(byteBuf);

                ByteBuf header = pkg.getHeader().copy();
                ByteBuf body = pkg.getBody().copy();

                switch (pkg.getPkgType()) {
                    case PackageType.PKG_CONNECT_REQ:
                        ConnectReqPackage connectReqPackage = new ConnectReqPackage();
                        connectReqPackage.setHeader(header);
                        connectReqPackage.setBody(body);
                        pkg = connectReqPackage;
                        break;
                    case PackageType.PKG_CONNECT_RESP:
                        ConnectRespPackage connectRespPackage = new ConnectRespPackage();
                        connectRespPackage.setHeader(header);
                        connectRespPackage.setBody(body);
                        pkg = connectRespPackage;
                        break;
                    case PackageType.PKG_CHANNEL_CLOSE:
                        ChannelClosePackage channelClosePackage = new ChannelClosePackage();
                        channelClosePackage.setHeader(header);
                        channelClosePackage.setBody(body);
                        pkg = channelClosePackage;
                        break;
                    case PackageType.PKG_DEFAULT_DATA:
                        DefaultDataPackage defaultDataPackage = new DefaultDataPackage();
                        defaultDataPackage.setHeader(header);
                        defaultDataPackage.setBody(body);
                        pkg = defaultDataPackage;
                        break;
                    default:
                        break;
                }
                LOG.debug("{}", pkg.getClass().toString());
                ctx.fireChannelRead(pkg);
                packageLen = 0;
            } else {
                return;
            }
        }

        if (in.readableBytes() >= 4) {
            packageLen = in.readInt();
            //LOG.debug("package len = {}, {}", packageLen, in.readableBytes());
        }
    }
}
