package com.villcore.net.proxy.v3.common;

import com.sun.xml.internal.ws.handler.HandlerException;
import com.villcore.net.proxy.v3.pkg.*;
import com.villcore.net.proxy.v3.pkg.Package;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * 发送服务接口
 */
public abstract class BasicWriteableImpl implements Writeable {
    public void write() {
        List<Package> writePackages = getWritePackages();

        for(Package pkg : writePackages) {
            if(canWrite()) {
                ByteBuf header = pkg.getHeader();
                ByteBuf body = pkg.getBody();

                int tunnelId = parseTunnelId(pkg);
//                Package pkg2 = new Package();
//                pkg2.setHeader(header.copy());
//                pkg2.setBody(body.copy());

                if(!write(pkg)) {
                    failWrite(pkg);
                    continue;
                }
                touch(tunnelId);
            }
        }

        if(!writePackages.isEmpty()) {
            flush();
            writePackages.clear();
            writePackages = null;
        }
    }

//    public static int parseTunnelId(Package pkg) {
//        int tunnelId = -1;
//
//        ByteBuf header = pkg.getHeader();
//        ByteBuf body = pkg.getBody();
//
//        switch (pkg.getPkgType()) {
//            case PackageType.PKG_CONNECT_REQ: {//do nothing
//                break;
//            }
//
//            case PackageType.PKG_CONNECT_RESP: { //convert connId pari
//                ConnectRespPackage connectRespPackage = new ConnectRespPackage();
//                connectRespPackage.setHeader(header);
//                connectRespPackage.setBody(body);
//
//                int connId = connectRespPackage.getLocalConnId();
//                tunnelId = connId;
//                connectRespPackage.toByteBuf().release();
//                break;
//            }
//
//            case PackageType.PKG_CHANNEL_CLOSE: {//convert connId pair
//                ChannelClosePackage channelClosePackage = new ChannelClosePackage();
//                channelClosePackage.setHeader(header);
//                channelClosePackage.setBody(body);
//
//                int connId = channelClosePackage.getLocalConnId();
//                tunnelId = connId;
//                channelClosePackage.toByteBuf().release();
//                break;
//            }
//
//            case PackageType.PKG_DEFAULT_DATA: {//convert connId pair
//                DefaultDataPackage defaultDataPackage = new DefaultDataPackage();
//                defaultDataPackage.setHeader(header);
//                defaultDataPackage.setBody(body);
//
//                int connId = defaultDataPackage.getLocalConnId();
//                tunnelId = connId;
//                defaultDataPackage.toByteBuf().release();
//                break;
//            }
//            default:
//                break;
//        }
//
//        PackageUtils.release(pkg.getFixed());
//        PackageUtils.release(pkg.getHeader());
//        PackageUtils.release(pkg.getBody());
//
//        return tunnelId;
//    }

    public static int parseTunnelId(Package pkg) {
        int tunnelId = -1;

        ByteBuf header = pkg.getHeader();
        ByteBuf body = pkg.getBody();

        switch (pkg.getPkgType()) {
            case PackageType.PKG_CONNECT_REQ: {//do nothing
                break;
            }

            case PackageType.PKG_CONNECT_RESP: { //convert connId pari
                ConnectRespPackage connectRespPackage = ConnectRespPackage.class.cast(pkg);
                int connId = connectRespPackage.getLocalConnId();
                tunnelId = connId;
                break;
            }

            case PackageType.PKG_CHANNEL_CLOSE: {//convert connId pair
                ChannelClosePackage channelClosePackage = ChannelClosePackage.class.cast(pkg);
                int connId = channelClosePackage.getLocalConnId();
                tunnelId = connId;
                break;
            }

            case PackageType.PKG_DEFAULT_DATA: {//convert connId pair
                DefaultDataPackage defaultDataPackage = DefaultDataPackage.class.cast(pkg);
                int connId = defaultDataPackage.getLocalConnId();
                tunnelId = connId;
                break;
            }
            default:
                break;
        }
        return tunnelId;
    }
}
