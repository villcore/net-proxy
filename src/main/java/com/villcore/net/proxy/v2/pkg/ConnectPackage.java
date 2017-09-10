package com.villcore.net.proxy.v2.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

public class ConnectPackage extends Package {
    {
        this.setPkgType(PackageType.PKG_CONNECT_REQ);
    }

    public void setLocalConnId(Integer localId) {

    }

    public void setRemoteConnId(Integer remoteId) {

    }

    public void setUserFlag(Long userFlag) {

    }

    public int getLocalConnectionId() {
        return header.getInt(0);
    }

    public static ByteBuf newHeader(String hostName, short port, int localConnId, long userFlag) throws UnsupportedEncodingException {
        //headerLen[4] + localConnId[4] + userFlag[8] + addrLen[4] + addr + port[2]
        byte[] addrBytes = hostName.getBytes("utf-8");
        //ByteBuf header = Unpooled.buffer(4 + 4 + 8 + 4 + addrBytes.length  + 2);
        ByteBuf header = Unpooled.buffer(4 + 8 + 4 + addrBytes.length  + 2);

        //header.writeInt(header.capacity());
        header.writeInt(localConnId);
        header.writeLong(userFlag);
        header.writeInt(addrBytes.length);
        header.writeBytes(addrBytes);
        header.writeShort(port);
        return header;
    }
}
