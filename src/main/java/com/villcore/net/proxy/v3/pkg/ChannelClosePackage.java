package com.villcore.net.proxy.v3.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

public class ChannelClosePackage extends Package {
    {
        this.setPkgType(PackageType.PKG_CHANNEL_CLOSE);
    }


    public int getLocalConnId() {
        int localConnId = -1;
        ByteBuf header = getHeader();
        int oriReadIndex = header.readerIndex();

        localConnId = header.readInt();
        header.readerIndex(oriReadIndex);

        return localConnId;
    }

    public int getRemoteConnId() {
        int remoteConnId = -1;
        ByteBuf header = getHeader();
        int oriReadIndex = header.readerIndex();

        header.readInt();
        remoteConnId = header.readInt();
        header.readerIndex(oriReadIndex);

        return remoteConnId;
    }

    public static ByteBuf newHeader(int localConnId, int remoteConnId, long userFlag){
        //localConnId[4] + remoteConnId[4] + userFlag[8];
        ByteBuf header = Unpooled.buffer(4 + 4 + 8);

        header.writeInt(localConnId);
        header.writeInt(remoteConnId);
        header.writeLong(userFlag);
        header.writerIndex(header.capacity());
        return header;
    }

    public static ChannelClosePackage valueOf2(ByteBuf byteBuf) {
        ChannelClosePackage pkg = new ChannelClosePackage();

        int readerIndex = byteBuf.readerIndex();

        int totalLen = byteBuf.getInt(readerIndex);
        int headerLen = byteBuf.getInt(readerIndex + 4);
        int bodyLen = byteBuf.getInt(readerIndex + 4 + 4);
        short pkgType = byteBuf.getShort(readerIndex + 4 + 4 + 4);

        ByteBuf fixed = byteBuf.slice(readerIndex, FIXED_LEN);
        ByteBuf header = byteBuf.slice(readerIndex + FIXED_LEN, headerLen);
        ByteBuf body = byteBuf.slice(readerIndex + FIXED_LEN + headerLen, bodyLen);

        pkg.setFixed(byteBuf.slice(0, FIXED_LEN));
        pkg.setHeader(header);
        pkg.setBody(body);
        return pkg;
    }
}
