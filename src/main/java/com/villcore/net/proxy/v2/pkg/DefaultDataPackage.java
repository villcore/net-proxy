package com.villcore.net.proxy.v2.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

public class DefaultDataPackage extends Package {
    {
        setPkgType(PackageType.PKG_DEFAULT_DATA);
    }
    public int getLocalConnectionId () {
        return header.getInt(0);
    }

    public static ByteBuf newHeader(int localConnId, int remoteConnId, long userFlag) throws UnsupportedEncodingException {
        //headerLen[4] + localConnId[4] + remoteConnid[4] + userFlag[8]
        //ByteBuf header = Unpooled.buffer(4 + 4 + 4 + 8);
        ByteBuf header = Unpooled.buffer(4 + 4 + 8);

        //header.writeInt(header.capacity());
        header.writeInt(localConnId);
        header.writeInt(remoteConnId);
        header.writeLong(userFlag);
        return header;
    }
}
