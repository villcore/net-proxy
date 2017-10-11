package com.villcore.net.proxy.v3.common;

import com.villcore.net.proxy.v3.pkg.v1.*;
import com.villcore.net.proxy.v3.pkg.v1.Package;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取ByteBuf并根据length信息构建 {@link Package}
 */
public class PackageDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PackageDecoder.class);

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (!(msg instanceof ByteBuf)) {
//            ctx.fireChannelRead(msg);
//            return;
//        }
//
//        ByteBuf in = (ByteBuf) msg;
//        //LOG.debug("rx = {}, wx = {}", in.readerIndex(), in.writerIndex());
//
////        System.out.println("byte buf ref = {}" + in.refCnt());
//        Package pkg = Package.valueOf2(in);
////        System.out.printf("pkg ref, fix = %d, header = %d, body = %d \n", pkg.getFixed().refCnt(), pkg.getHeader().refCnt(), pkg.getBody().refCnt());
//
//        //PackageUtils.printRef(this.getClass().getSimpleName(), pkg);
//
//        ByteBuf header = pkg.getHeader();
//        ByteBuf body = pkg.getBody();
//        short pkgType = pkg.getPkgType();
//
//        switch (pkgType) {
//            case PackageType.PKG_CONNECT_REQ:
//                ConnectReqPackage connectReqPackage = new ConnectReqPackage();
//                //LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
//                connectReqPackage.setHeader(header);
//                connectReqPackage.setBody(body);
//                PackageUtils.printRef(this.getClass().getSimpleName(), pkg);
//
//                //LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", connectReqPackage.getHostname(), connectReqPackage.getPort());
//                pkg = connectReqPackage;
//                break;
//            case PackageType.PKG_CONNECT_RESP:
//                ConnectRespPackage connectRespPackage = new ConnectRespPackage();
//                //LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
//                connectRespPackage.setHeader(header);
//                connectRespPackage.setBody(body);
//                //                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", connectRespPackage.getLocalConnId(), connectRespPackage.getRemoteConnId());
//                pkg = connectRespPackage;
//                break;
//            case PackageType.PKG_CHANNEL_CLOSE:
//                ChannelClosePackage channelClosePackage = new ChannelClosePackage();
//                //                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
//                channelClosePackage.setHeader(header);
//                channelClosePackage.setBody(body);
//                //                        LOG.debug(">>>>>>>>>>>>>>>locaConnId = {}, remoteId = {}", connectRespPackage.getLocalConnId(), connectRespPackage.getRemoteConnId());
//                pkg = channelClosePackage;
//                break;
//            case PackageType.PKG_DEFAULT_DATA:
//                DefaultDataPackage defaultDataPackage = new DefaultDataPackage();
//                defaultDataPackage.setHeader(header);
//                defaultDataPackage.setBody(body);
//                pkg = defaultDataPackage;
//                //LOG.debug(">>>>>>>>>>>>>>>data locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
//                break;
//            case PackageType.PKG_CONNECTION_AUTH_REQ:
//                ConnectAuthReqPackage connectAuthReqPackage = new ConnectAuthReqPackage();
//                connectAuthReqPackage.setHeader(header);
//                connectAuthReqPackage.setBody(body);
//                pkg = connectAuthReqPackage;
//                //LOG.debug(">>>>>>>>>>>>>>>data locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
//                break;
//            case PackageType.PKG_CONNECTION_AUTH_RESP:
//                ConnectAuthRespPackage connectAuthRespPackage = new ConnectAuthRespPackage();
//                connectAuthRespPackage.setHeader(header);
//                connectAuthRespPackage.setBody(body);
//                pkg = connectAuthRespPackage;
//                //LOG.debug(">>>>>>>>>>>>>>>data locaConnId = {}, remoteId = {}", in.getInt(in.readerIndex()), in.getInt(in.readerIndex() + 4));
//                break;
//            default:
//                break;
//        }
//
//        PackageUtils.printRef("decode package >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ", pkg);
//        //LOG.debug("recv pkg, totalLen = {}, headerLen = {}, bodyLen = {}", pkg.getTotalLen(), pkg.getHeaderLen(), pkg.getBodyLen());
//        //PackageUtils.printRef(this.getClass().getSimpleName() + "2", pkg);
//
//        ctx.fireChannelRead(pkg);
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf in = (ByteBuf) msg;
        int readerIndex = in.readerIndex();
        int FIXED_LEN = 4 + 4 + 4 + 2;

        ByteBuf fixed = in.slice(readerIndex, FIXED_LEN);

        int totalLen = fixed.getInt(readerIndex);
        int headerLen = fixed.getInt(readerIndex + 4);
        int bodyLen = fixed.getInt(readerIndex + 4 + 4);
        short pkgType = fixed.getShort(readerIndex + 4 + 4 + 4);

        ByteBuf header = in.slice(readerIndex + FIXED_LEN, headerLen);
        ByteBuf body = in.slice(readerIndex + FIXED_LEN + headerLen, bodyLen);

        Package pkg = null;
        switch (pkgType) {
            case PackageType.PKG_CONNECT_REQ:
                //不做处理
                pkg = ConnectReqPackage.valueOf2(in);
                break;

            case PackageType.PKG_CONNECT_RESP:
                pkg = correctConnectRespPackage(in, fixed, header, body);
                break;

            case PackageType.PKG_CHANNEL_CLOSE:
                pkg = correctChannelClosePackage(in, fixed, header, body);
                break;

            case PackageType.PKG_DEFAULT_DATA:
                pkg = correctDataPackage(in, fixed, header, body);
                break;

            case PackageType.PKG_CONNECTION_AUTH_REQ:
                break;

            case PackageType.PKG_CONNECTION_AUTH_RESP:
                break;
            default:
                break;
        }
        if(pkg != null) {
            ctx.fireChannelRead(pkg);
        } else {
            LOG.error("decode null package ...");
        }
    }

//    private short getPackageType(ByteBuf byteBuf) {
//        short pkgType = byteBuf.getShort(byteBuf.readerIndex() + 4 + 4 + 4);
//        return pkgType;
//    }

    private ConnectRespPackage correctConnectRespPackage(ByteBuf all, ByteBuf fixed, ByteBuf header, ByteBuf body) {
        int headerReaderIndex = header.readerIndex();
        int connId = header.getInt(headerReaderIndex);
        int corrspondConnId = header.getInt(headerReaderIndex + 4);

        int oriWriteIndex = header.writerIndex();
        header.writerIndex(headerReaderIndex);
        header.writeInt(corrspondConnId).writeInt(connId);
        header.writerIndex(oriWriteIndex);

        return ConnectRespPackage.valueOf2(all);
    }

    private DefaultDataPackage correctDataPackage(ByteBuf all, ByteBuf fixed, ByteBuf header, ByteBuf body) {
        int headerReaderIndex = header.readerIndex();
        int connId = header.getInt(headerReaderIndex);
        int corrspondConnId = header.getInt(headerReaderIndex + 4);

        int oriWriteIndex = header.writerIndex();
        header.writerIndex(headerReaderIndex);
        header.writeInt(corrspondConnId).writeInt(connId);
        header.writerIndex(oriWriteIndex);

        return DefaultDataPackage.valueOf2(all);
    }

    private ChannelClosePackage correctChannelClosePackage(ByteBuf all, ByteBuf fixed, ByteBuf header, ByteBuf body) {
        System.out.println(header.refCnt());
        int headerReaderIndex = header.readerIndex();
        int connId = header.getInt(headerReaderIndex);
        int corrspondConnId = header.getInt(headerReaderIndex + 4);

        int oriWriteIndex = header.writerIndex();
        header.writerIndex(headerReaderIndex);
        header.writeInt(corrspondConnId).writeInt(connId);
        header.writerIndex(oriWriteIndex);

        return ChannelClosePackage.valueOf2(all);
    }
}
