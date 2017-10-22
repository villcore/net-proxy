package com.villcore.net.proxy.v3.pkg.v2;

import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;

import java.nio.ByteBuffer;

public class TransferPackage extends Package {
    {
        setPkgType(PackageType.PKG_WRAP_TRANSFER);
    }

    //header(oriPkgType, compressType, ivLen, ivBytes)
    //body (oriPackage bytes)

    public short getWrapPackageType() {
        byte[] header = getHeader();
        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        short wrapPkgType = byteBuffer.getShort(0);
        return wrapPkgType;
    }

    public short getCompressType() {
        byte[] header = getHeader();
        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        short wrapPkgType = byteBuffer.getShort(2);
        return wrapPkgType;
    }

    public byte[] getIvBytes() {
        byte[] header = getHeader();
        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        short ivBytesLen = byteBuffer.getShort(2 + 2);
//        System.out.println("header len = " + header.length + " iv bytes len = " + ivBytesLen);
//        System.out.println(byteBuffer.getShort());
//        System.out.println(byteBuffer.getShort(2));
//        System.out.println(byteBuffer.getShort(2 + 2));

        byte[] ivBytes = new byte[ivBytesLen];
        byteBuffer.position(2 + 2 + 2);
        byteBuffer.get(ivBytes);
        return ivBytes;
    }

    public static byte[] newHeader(Package wrapedPackage, short compressType, byte[] ivBytes) {
        byte[] header = new byte[2 + 2 + 2 + ivBytes.length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        byteBuffer.putShort(0, wrapedPackage.getPkgType());
        byteBuffer.putShort(2, compressType);
        byteBuffer.putShort(2 + 2, (short) ivBytes.length);
        byteBuffer.position(2 + 2 + 2);
        byteBuffer.put(ivBytes);
        return header;
    }

    public static byte[] newHeader(short pkgType, short compressType, byte[] ivBytes) {
        byte[] header = new byte[2 + 2 + 2 + ivBytes.length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        byteBuffer.putShort(0, pkgType);
        byteBuffer.putShort(2, compressType);
        byteBuffer.putShort(2 + 2, (short) ivBytes.length);
//        System.out.println("iv bytes len = " + ivBytes.length);

        byteBuffer.position(2 + 2 + 2);
        byteBuffer.put(ivBytes);

//        System.out.println("header len = " + header.length + " iv bytes len = " + ivBytes.length);
//        System.out.println(byteBuffer.getShort(0));
//        System.out.println(byteBuffer.getShort(2));
//        System.out.println(byteBuffer.getShort(2 + 2));
        return header;
    }
}
