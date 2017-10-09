package com.villcore.net.proxy.v3.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

public class DefaultDataPackage extends Package {
    {
        setPkgType(PackageType.PKG_DEFAULT_DATA);
    }

    public int getLocalConnId() {
        int localConnId = -1;
        ByteBuf header = getHeader();
        int oriReadIndex = header.readerIndex();

        localConnId = header.getInt(oriReadIndex);
        header.readerIndex(oriReadIndex);

        return localConnId;
    }

    public int getRemoteConnId() {
        int localConnId = -1;
        ByteBuf header = getHeader();
        int oriReadIndex = header.readerIndex();
        localConnId = header.getInt(oriReadIndex + 4);
        header.readerIndex(oriReadIndex);

        return localConnId;
    }

    public void setRemoteConnId(int remoteConnId) {
        ByteBuf header = getHeader();
        int oriReadIndex = header.readerIndex();
        header.setInt(oriReadIndex + 4, remoteConnId);
        header.readerIndex(oriReadIndex);
    }

    public static ByteBuf newHeader(int localConnId, int remoteConnId, long userFlag) {
        //localConnId[4] + remoteConnid[4] + userFlag[8]
        ByteBuf header = Unpooled.buffer(4 + 4 + 8);

        header.writeInt(localConnId);
        header.writeInt(remoteConnId);
        header.writeLong(userFlag);
        return header;
    }
}
