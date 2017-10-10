package com.villcore.net.proxy.v3.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
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

//    public static String toString(Package pkg) throws UnsupportedEncodingException {
//        return pkg.toByteBuf().copy().toString(Charset.forName("utf-8"));
//    }

    public static String toString(ByteBuf byteBuf) throws UnsupportedEncodingException {
        //ByteBuf byteBuf2 = byteBuf.copy();
        return byteBuf.toString(Charset.forName("utf-8"));
    }

    public static void release(Package pkg) {
        ByteBuf fix = pkg.getFixed();
        ByteBuf header = pkg.getHeader();
        ByteBuf body = pkg.getBody();

//        ReferenceCountUtil.release(fix);
//        ReferenceCountUtil.release(header);
//        ReferenceCountUtil.release(body);

        int fixRef = fix.refCnt();
        int headerFix = header.refCnt();
        int bodyFix = body.refCnt();

        try {
//            fix.release();
//            header.release();
//            body.release();
//            body.release(bodyFix);
//            release2(fix);
//            release2(header);
//            release2(body);

//            if((fix == header) &&  (header == body)) {
//                release2(fix);
//            }
//
//            if(header == body) {
//                release2(fix);
//                release2(header);
//            }
//
            release2(fix);
            release2(header);
//\            release2(body);
//            LOG.debug("pkg {} part hashCode, fix = {}, header = {}, body = {}", new Object[]{pkg.getClass(), fix.hashCode(), header.hashCode(), body.hashCode()});
            LOG.debug("pkg class = {}, fix ref = {}/{}, header ref = {}/{}, body ref = {}/{}", new Object[]{pkg.getClass(), fixRef, fix.refCnt(), headerFix, header.refCnt(), bodyFix, body.refCnt()});
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            LOG.debug("! exception pkg class = {}, fix ref = {}, header ref = {}, body ref = {}", new Object[]{pkg.getClass(), fix.refCnt(), header.refCnt(), body.refCnt()});
        }

    }

    public static void release(ByteBuf byteBuf) {
        if (byteBuf != null) {
            release2(byteBuf);
        }
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

//        LOG.debug("========================= before {}", byteBuf.refCnt());
        if(byteBuf.refCnt() > 0  && !byteBuf.release()) {
            byteBuf.release();
        }
//        LOG.debug("========================= after {}", byteBuf.refCnt());

//        while (!byteBuf.release()) {
//            byteBuf.release(1);
//            LOG.debug("xxxxxxxxxxxxxxxxxxxxxxxxxs {}", byteBuf.refCnt());
//        }
//        if (byteBuf != null) {
//            while (byteBuf.refCnt() > 0 && !byteBuf.release()) {
//                byteBuf.release();
//            }
//        }
    }

    public static void printRef(String curTag, Package pkg) {
        LOG.debug("cur tag = {}, package class = {}, package ref, fix = {}, header = {}, body = {}", new Object[]{curTag, pkg.getClass().getSimpleName(), pkg.getFixed().refCnt(), pkg.getHeader().refCnt(), pkg.getBody().refCnt()});
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
        if (correctPkg != pkg) {
            correctPkg.setHeader(header);
            correctPkg.setBody(body);
        }
        return correctPkg;
    }
}
