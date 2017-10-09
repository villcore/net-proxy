package com.villcore.net.proxy.v3.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class PackageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PackageUtils.class);

    public static ConnectReqPackage buildConnectPackage(String hostName, short port, int localConnId, long userFlag) throws UnsupportedEncodingException {
        ByteBuf header = ConnectReqPackage.newHeader(hostName, port, localConnId, userFlag);

        ConnectReqPackage pkg = new ConnectReqPackage();
        pkg.setHeader(header);
        pkg.setBody(Unpooled.EMPTY_BUFFER);
        return pkg;
    }

    public static DefaultDataPackage buildDataPackage(int localConnId, int remoteConnId, long userFlag, ByteBuf data) {
        ByteBuf header = DefaultDataPackage.newHeader(localConnId, remoteConnId, userFlag);
        header.writerIndex(header.capacity());
        header.readerIndex(0);

        DefaultDataPackage pkg = new DefaultDataPackage();
        pkg.setHeader(header);
        pkg.setBody(data);
        return pkg;
    }

    public static ConnectRespPackage buildConnectRespPackage(int localConnId, int remoteConnId, long userFlag) {
        ByteBuf header = ConnectRespPackage.newHeader(localConnId, remoteConnId, userFlag);
        header.writerIndex(header.capacity());
        header.readerIndex(0);

        ConnectRespPackage pkg = new ConnectRespPackage();
        pkg.setHeader(header);
        pkg.setBody(Unpooled.EMPTY_BUFFER);
        return pkg;
    }

    public static ChannelClosePackage buildChannelClosePackage(int localConnId, int remoteConnId, long userFlag) {
        ByteBuf header = ChannelClosePackage.newHeader(localConnId, remoteConnId, userFlag);
        header.writerIndex(header.capacity());
        header.readerIndex(0);

        ChannelClosePackage pkg = new ChannelClosePackage();
        pkg.setHeader(header);
        pkg.setBody(Unpooled.EMPTY_BUFFER);
        return pkg;
    }

    public static String toString(Package pkg) throws UnsupportedEncodingException {
        return pkg.toByteBuf().copy().toString(Charset.forName("utf-8"));
    }

    public static String toString(ByteBuf byteBuf) throws UnsupportedEncodingException {
        //ByteBuf byteBuf2 = byteBuf.copy();
        return byteBuf.toString(Charset.forName("utf-8"));
    }

    public static void release(Package pkg) {
        ByteBuf fix = pkg.getFixed();
        ByteBuf header = pkg.getHeader();
        ByteBuf body = pkg.getBody();
//        release(fix);
//        release(header);
//        release(body);
    }

    public static void release(ByteBuf byteBuf) {
//        if(byteBuf != null) {
//            LOG.debug("byteBuf is release {}, refCnt = {}", byteBuf.refCnt());
//
//            while (byteBuf.refCnt() > 0) {
//                byteBuf.release(1);
//            }
//        }
    }

    public static void release2(Package pkg) {
//        ByteBuf fix = pkg.getFixed();
//        ByteBuf header = pkg.getHeader();
//        ByteBuf body = pkg.getBody();
//        release2(fix);
//        release2(header);
//        release2(body);
    }

    public static void release2(ByteBuf byteBuf) {
//        if(byteBuf != null) {
//            LOG.debug("byteBuf is release {}, refCnt = {}", byteBuf.refCnt());
//
//            while (byteBuf.refCnt() > 0) {
//                byteBuf.release(1);
//            }
//        }
    }

    public static Package convertCorrectPackage(Package pkg) {
        ByteBuf header = pkg.getHeader();
        ByteBuf body = pkg.getBody();

        Package correctPkg = null;

        switch (pkg.getPkgType()) {
            case PackageType.PKG_BASIC:
                break;
            case PackageType.PKG_CONNECT_REQ:
                correctPkg = new ConnectReqPackage();
                break;
            case PackageType.PKG_CONNECT_RESP:
                correctPkg = new ConnectRespPackage();
                break;
            case PackageType.PKG_DEFAULT_DATA:
                correctPkg = new DefaultDataPackage();
                break;
            case PackageType.PKG_CHANNEL_CLOSE:
                correctPkg = new ChannelClosePackage();
                break;
                default:
                    correctPkg = pkg;
                    break;
        }
        if(correctPkg != pkg) {
            correctPkg.setHeader(header);
            correctPkg.setBody(body);
        }
        return correctPkg;
    }
}
