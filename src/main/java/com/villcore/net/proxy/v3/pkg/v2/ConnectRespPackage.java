package com.villcore.net.proxy.v3.pkg.v2;

import java.nio.ByteBuffer;

public class ConnectRespPackage extends com.villcore.net.proxy.v3.pkg.v2.Package {
    {
        this.setPkgType(PackageType.PKG_CONNECT_RESP);
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
