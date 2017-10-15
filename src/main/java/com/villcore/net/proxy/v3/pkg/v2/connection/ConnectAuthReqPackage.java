//package com.villcore.net.proxy.v3.pkg.v2.connection;
//
//import com.villcore.net.proxy.v3.pkg.v2.PackageType;
//import com.villcore.net.proxy.v3.pkg.v2.Package;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//
//import java.io.UnsupportedEncodingException;
//
//public class ConnectAuthReqPackage extends Package {
//    //userFlag 4byte long
//    //password wordLen 2 byte + len
//    //authId   4byte long
//    {
//        this.setPkgType(PackageType.PKG_CONNECTION_AUTH_REQ);
//    }
//
//    public long getUserFlag() {
//        long userFlag = -1L;
//        ByteBuf header = getHeader();
//        int oriRederIndex = header.readerIndex();
//
//        userFlag = header.readLong();
//        header.readerIndex(oriRederIndex);
//        return userFlag;
//    }
//
//    public String getPassword() {
//        String password = "";
//
//        ByteBuf header = getHeader();
//        int oriRederIndex = header.readerIndex();
//
//        header.readLong();
//        short passwordLen = header.readShort();
//        byte[] hostnameBytes = new byte[passwordLen];
//        header.readBytes(hostnameBytes);
//        header.readerIndex(oriRederIndex);
//
//        try {
//            password = new String(hostnameBytes, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } finally {
//            header.readerIndex(oriRederIndex);
//        }
//        return password;
//    }
//
//    public long getAuthId() {
//        long authId = -1L;
//
//        ByteBuf header = getHeader();
//        int oriRederIndex = header.readerIndex();
//
//        header.readLong();
//        short passwordLen = header.readShort();
//        byte[] hostnameBytes = new byte[passwordLen];
//        header.readBytes(hostnameBytes);
//        authId = header.readLong();
//        header.readerIndex(oriRederIndex);
//
//        return authId;
//    }
//
//
//    public static ByteBuf newHeader(long userFlag, String password, long authId) throws UnsupportedEncodingException {
//        //
//        byte[] passwordBytes = password.getBytes("utf-8");
//
//        ByteBuf header = Unpooled.buffer(8 + 2 + passwordBytes.length + 8);
//
//        header.writeLong(userFlag);
//        header.writeShort(passwordBytes.length);
//        header.writeBytes(passwordBytes);
//        header.writeLong(authId);
//        header.writerIndex(header.capacity());
//        return header;
//    }
//}
