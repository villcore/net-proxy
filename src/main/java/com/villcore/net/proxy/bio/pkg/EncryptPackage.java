package com.villcore.net.proxy.bio.pkg;

import com.villcore.net.proxy.bio.util.ByteArrayUtils;

import java.nio.ByteBuffer;

/**
 * 加密Package
 *
 * 10随机字节 + 随机字节编码的真实头部 + 加密的body + 额外数据用来干扰
 */
public class EncryptPackage extends Package {
    //[(bodySize + 干扰数据Size) / (bodySize) /（userFlag) / ivBytes] [ body (加密内容（defaultPackage))] [(干扰数据)]

    @Override
    public int getSize(byte[] header) {
        return ByteBuffer.wrap(header).getInt();
    }

    @Override
    public long getUserFlag(byte[] header) {
        return ByteBuffer.wrap(header).getLong(4 + 4);
    }

    @Override
    public void setUserFlag(byte[] header, long userFlag) {
        ByteBuffer.wrap(header).putLong(4 + 4, userFlag);
    }

    @Override
    public int setSize(byte[] header, int size) {
        ByteBuffer.wrap(header).putInt(size);
        return size;
    }

    @Override
    public int getHeaderLen() {
        return PkgConf.getEndryptPackageHeaderLen();
    }

    public int getBodySize(byte[] header) {
        return ByteBuffer.wrap(header).getInt(4);
    }

    public byte[] getIvBytes(byte[] header) {
        byte[] ivBytes = new byte[PkgConf.getIvBytesLen()];
        ByteArrayUtils.cpyToNew(header, ivBytes, 4 + 4 + 8, 0, ivBytes.length);
        return ivBytes;
    }
}
