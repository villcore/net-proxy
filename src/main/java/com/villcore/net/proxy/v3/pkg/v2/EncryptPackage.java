//package com.villcore.net.proxy.v3.pkg.v2;
//
//import java.nio.ByteBuffer;
//
//public class EncryptPackage extends Package {
//    {
//        setPkgType(PackageType.PKG_WRAP_TRANSFER);
//    }
//
//    //header(oriPkgType, compressType, ivLen, ivBytes)
//    //body (oriPackage bytes)
//
//    public short getWrapPackageType() {
//        byte[] header = getHeader();
//        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
//        short wrapPkgType = byteBuffer.getShort(0);
//        return wrapPkgType;
//    }
//
//    public short getCompressType() {
//        byte[] header = getHeader();
//        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
//        short wrapPkgType = byteBuffer.getShort(2);
//    }
//
//    public short getEncryptType() {
//        return -1;
//    }
//
//    public byte[] encryptMeta() {
//
//        return new byte[0];
//    }
//
//}
