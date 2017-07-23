package com.villcore.net.proxy.bio.pkg2;

import java.nio.ByteBuffer;

/**
 * Created by villcore on 2017/7/18.
 */
public class UserPackage extends Package {
    public int getConnectionId(){
        return ByteBuffer.wrap(getHeader()).getInt();
    }

    public long getUserFlag(){
        return ByteBuffer.wrap(getHeader()).getLong(PkgConf.getUserPackageHeaderLen() - 8);
    }

    public void setConnectionId(int connectionId){
        ByteBuffer.wrap(getHeader()).putInt(connectionId);
    }

    public void setUserFlag(long userFlag){
        ByteBuffer.wrap(getHeader()).putLong(PkgConf.getUserPackageHeaderLen() - 8, userFlag);
    }

    @Override
    public byte[] newHeader() {
        return new byte[PkgConf.getUserPackageHeaderLen()];
    }
}
