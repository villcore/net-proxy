package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.connection.ConnectAuthReqPackage;
import com.villcore.net.proxy.v3.pkg.connection.ConnectAuthRespPackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 读取ByteBuf并根据length信息构建 {@link Package}
 */
public class PackageDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PackageDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf in = (ByteBuf) msg;
        //LOG.debug("rx = {}, wx = {}", in.readerIndex(), in.writerIndex());

        Package pkg = Package.valueOf2(in);

        //PackageUtils.printRef(this.getClass().getSimpleName(), pkg);

        ByteBuf header = pkg.getHeader();
        ByteBuf body = pkg.getBody();
        short pkgType = pkg.getPkgType();

        switch (pkgType) {
            case PackageType.PKG_CONNECT_REQ:
                ConnectReqPackage connectReqPackage = new ConnectReqPackage();
                //LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
                connectReqPackage.setHeader(header);
                connectReqPackage.setBody(body);
                PackageUtils.printRef(this.getClass().getSimpleName(), pkg);

                //LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", connectReqPackage.getHostname(), connectReqPackage.getPort());
                pkg = connectReqPackage;
                break;
            case PackageType.PKG_CONNECT_RESP:
                ConnectRespPackage connectRespPackage = new ConnectRespPackage();
                //LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
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
                //LOG.debug(">>>>>>>>>>>>>>>data locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
                break;
            case PackageType.PKG_CONNECTION_AUTH_REQ:
                ConnectAuthReqPackage connectAuthReqPackage = new ConnectAuthReqPackage();
                connectAuthReqPackage.setHeader(header);
                connectAuthReqPackage.setBody(body);
                pkg = connectAuthReqPackage;
                //LOG.debug(">>>>>>>>>>>>>>>data locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
                break;
            case PackageType.PKG_CONNECTION_AUTH_RESP:
                ConnectAuthRespPackage connectAuthRespPackage = new ConnectAuthRespPackage();
                connectAuthRespPackage.setHeader(header);
                connectAuthRespPackage.setBody(body);
                pkg = connectAuthRespPackage;
                //LOG.debug(">>>>>>>>>>>>>>>data locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
                break;
            default:
                break;
        }

        //LOG.debug("recv pkg, totalLen = {}, headerLen = {}, bodyLen = {}", pkg.getTotalLen(), pkg.getHeaderLen(), pkg.getBodyLen());
        //PackageUtils.printRef(this.getClass().getSimpleName() + "2", pkg);

        ctx.fireChannelRead(pkg);
    }
}
