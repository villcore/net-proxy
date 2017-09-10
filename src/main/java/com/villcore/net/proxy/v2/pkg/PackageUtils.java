package com.villcore.net.proxy.v2.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

public class PackageUtils {
    public static Package buildConnectPackage(String hostName, short port, int localConnId, long userFlag) throws UnsupportedEncodingException {
        ByteBuf header = ConnectPackage.newHeader(hostName, port, localConnId, userFlag);

        Package pkg = new ConnectPackage();
        pkg.setHeader(header);
        pkg.setBody(Unpooled.EMPTY_BUFFER);
        return pkg;
    }

    public static Package buildDataPackage(int localConnId, int remoteConnId, long userFlag, ByteBuf data) throws UnsupportedEncodingException {
        ByteBuf header = DefaultDataPackage.newHeader(localConnId, remoteConnId, userFlag);

        Package pkg = new DefaultDataPackage();
        pkg.setHeader(header);
        pkg.setBody(data);
        return pkg;
    }
}
