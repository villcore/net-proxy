package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 此类将服务端接收到的 ByteBuf 还原成对应的Package, 该类不可共享
 */
public class ClientPackageDecoder extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(ClientPackageDecoder.class);

    private static AtomicLong cnt = new AtomicLong();

    private int packageLen = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //LOG.debug("rx = {}, wx = {}", in.readerIndex(), in.writerIndex());
        if (packageLen > 0) {
//            LOG.debug("pkg len = {}", packageLen);
            if (in.readableBytes() >= packageLen - 4) {
                ByteBuf byteBuf = in.slice(in.readerIndex(), packageLen - 4);
                in.readerIndex(in.readerIndex() + packageLen - 4);

//                LOG.debug("rx = {}, wx = {}", in.readerIndex(), in.writerIndex());
//                LOG.debug("rx = {}, wx = {}", byteBuf.readerIndex(), byteBuf.writerIndex());

                Package pkg = Package.valueOf(byteBuf);

                ByteBuf header = pkg.getHeader().copy();
                ByteBuf body = pkg.getBody().copy();

                switch (pkg.getPkgType()) {
                    case PackageType.PKG_CONNECT_REQ:
                        ConnectReqPackage connectReqPackage = new ConnectReqPackage();
//                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
                        connectReqPackage.setHeader(header);
                        connectReqPackage.setBody(body);
//                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", connectRespPackage.getLocalConnId(), connectRespPackage.getRemoteConnId());
                        pkg = connectReqPackage;
                        break;
                    case PackageType.PKG_CONNECT_RESP:
                        ConnectRespPackage connectRespPackage = new ConnectRespPackage();
//                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
                        connectRespPackage.setHeader(header);
                        connectRespPackage.setBody(body);
//                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", connectRespPackage.getLocalConnId(), connectRespPackage.getRemoteConnId());
                        pkg = connectRespPackage;
                        break;
                    case PackageType.PKG_CHANNEL_CLOSE:
                        ChannelClosePackage channelClosePackage = new ChannelClosePackage();
//                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
                        channelClosePackage.setHeader(header);
                        channelClosePackage.setBody(body);
//                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", connectRespPackage.getLocalConnId(), connectRespPackage.getRemoteConnId());
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

//                LOG.debug("recv pkg, totalLen = {}, headerLen = {}, bodyLen = {}", pkg.getTotalLen(), pkg.getHeaderLen(), pkg.getBodyLen());
                //out.add(pkg);
                ctx.fireChannelRead(pkg);
                //LOG.debug("decoder recv pgk count = {}", cnt.incrementAndGet());
                packageLen = 0;
            } else {
                //continue;
                return;
            }
        }

        if (in.readableBytes() >= 4) {
            packageLen = in.readInt();
            //System.out.println("pkg len = " + packageLen);
        }
    }
}
