package com.villcore.net.proxy.bio.pkg2;

import com.villcore.net.proxy.bio.util.ByteArrayUtils;

import java.nio.ByteBuffer;

/**
 * 加密Package
 *
 * 10随机字节 + 随机字节编码的真实头部 + 加密的body + 额外数据用来干扰
 */
public class TransferPackage extends Package {
//    @Override
//    public int getSize(byte[] header) {
//        return ByteBuffer.wrap(header).getInt();
//    }
//
//    @Override
//    public long getUserFlag(byte[] header) {
//        return -1;
//    }
//
//    @Override
//    public void setUserFlag(byte[] header, long userFlag) {
//    }
//
//    @Override
//    public int setSize(byte[] header, int size) {
//        ByteBuffer.wrap(header).putInt(size);
//        return size;
//    }
//
//    public static TransferPackage wrap(Package pkg) {
//        byte[] header = pkg.getHeader();
//        byte[] body = pkg.getBody();
//        int size = header.length + body.length;
//
//        byte[] all = new byte[size];
//        ByteArrayUtils.cpyToNew(header, all, 0, 0, header.length);
//        ByteArrayUtils.cpyToNew(body, all, 0, header.length, body.length);
//
//        byte[] newHeader = ByteBuffer.wrap(new byte[4]).putInt(size).array();
//
//        TransferPackage transferPackage = new TransferPackage();
//        transferPackage.setHeader(newHeader);
//        transferPackage.setBody(all);
//        transferPackage.setSize(newHeader, size);
//        return transferPackage;
//    }
//    @Override
//    public int getHeaderLen() {
//        return PkgConf.getTransferPackageHeaderLen();
//    }
}
