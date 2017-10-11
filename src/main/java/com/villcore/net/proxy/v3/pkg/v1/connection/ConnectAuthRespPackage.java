package com.villcore.net.proxy.v3.pkg.v1.connection;

import com.villcore.net.proxy.v3.pkg.v1.Package;
import com.villcore.net.proxy.v3.pkg.v1.PackageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

public class ConnectAuthRespPackage extends Package {
    //authId
    {
        setPkgType(PackageType.PKG_CONNECTION_AUTH_RESP);
    }

    public long getAuthId() {
        long authId = -1L;

        ByteBuf header = getHeader();
        int oriRederIndex = header.readerIndex();
        authId = header.readLong();
        header.readerIndex(oriRederIndex);

        return authId;
    }


    public static ByteBuf newHeader(long authId) throws UnsupportedEncodingException {
        ByteBuf header = Unpooled.buffer(8);
        header.writeLong(authId);
        header.writerIndex(header.capacity());
        return header;
    }
}
