package com.villcore.net.proxy.bio.pkg;

import com.villcore.net.proxy.bio.pkg.Package;

import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/18.
 */
public class DefaultPackage extends Package {
    @Override
    public int getSize(byte[] header) {
        return ByteBuffer.wrap(header).getInt();
    }

    @Override
    public long getUserFlag(byte[] header) {
        return -1L;
    }

    @Override
    public void setUserFlag(byte[] header, long userFlag) {
    }

    @Override
    public int setSize(byte[] header, int size) {
        ByteBuffer.wrap(header).putInt(size);
        return size;
    }

    @Override
    public int getHeaderLen() {
        return PkgConf.getDefaultPackageHeaderLen();
    }
}
