package com.villcore.net.proxy.v3.pkg.v2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public class DefaultDataPackage extends com.villcore.net.proxy.v3.pkg.v2.Package {
    {
        setPkgType(PackageType.PKG_DEFAULT_DATA);
    }

    public int getLocalConnId() {
        byte[] headerBytes = getHeader();
        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        return header.getInt();
    }

    public int getRemoteConnId() {
        byte[] headerBytes = getHeader();
        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        return header.getInt(4);
    }

    public static byte[] newHeader(int localConnId, int remoteConnId, long userFlag) {
        //localConnId[4] + remoteConnId[4] + userFlag[8];
        byte[] headerBytes = new byte[4 + 4 + 8];
        ByteBuffer header = ByteBuffer.wrap(headerBytes);

        header.putInt(localConnId);
        header.putInt(4, remoteConnId);
        header.putLong(4 + 4, userFlag);
        return headerBytes;
    }
}
