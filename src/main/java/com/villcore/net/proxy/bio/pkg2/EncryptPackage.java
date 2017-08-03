package com.villcore.net.proxy.bio.pkg2;

import com.villcore.net.proxy.bio.util.ByteArrayUtils;

import java.nio.ByteBuffer;

/**
 * 加密Package
 */
public class EncryptPackage extends Package {
    public long getUserFlag() {
        return ByteBuffer.wrap(getHeader()).getLong(4 + 4);
    }

    public int getBodySize() {
        return ByteBuffer.wrap(getHeader()).getInt(4);
    }

    public int getHeaderSize() {
        return ByteBuffer.wrap(getHeader()).getInt();
    }

    public byte[] getIvBytes() {
        byte[] ivBytes = new byte[PkgConf.getIvBytesLen()];
        ByteArrayUtils.cpyToNew(getHeader(), ivBytes, 4 + 4 + 8, 0, ivBytes.length);
        return ivBytes;
    }

    @Override
    public byte[] newHeader() {
        return new byte[PkgConf.getEndryptPackageHeaderLen()];
    }
}
