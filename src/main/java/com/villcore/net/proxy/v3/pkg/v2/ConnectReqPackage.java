package com.villcore.net.proxy.v3.pkg.v2;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ConnectReqPackage extends com.villcore.net.proxy.v3.pkg.v2.Package {
    {
        this.setPkgType(PackageType.PKG_CONNECT_REQ);
    }

    public String getHostname() {
        String hostname = "www.google.com";
        byte[] header = getHeader();
        ByteBuffer headerBuffer = ByteBuffer.wrap(header);

        int connId = headerBuffer.getInt();
        long userFlag = headerBuffer.getLong(4);
        int addrLen = headerBuffer.getInt(4 + 8);
        byte[] addrBytes = new byte[addrLen];

        System.arraycopy(header, 4 + 8 + 4, addrBytes, 0, addrLen);
        try {
            hostname = new String(addrBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        short port = headerBuffer.getShort(4 + 8 + 4 + addrLen);
        return hostname;
    }

    public short getPort() {
        short port = -1;
        String hostname = "www.google.com";
        byte[] header = getHeader();
        ByteBuffer headerBuffer = ByteBuffer.wrap(header);

        int connId = headerBuffer.getInt();
        long userFlag = headerBuffer.getLong(4);
        int addrLen = headerBuffer.getInt(4 + 8);
        byte[] addrBytes = new byte[addrLen];

        System.arraycopy(header, 4 + 8 + 4, addrBytes, 0, addrLen);
        try {
            hostname = new String(addrBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        port = headerBuffer.getShort(4 + 8 + 4 + addrLen);
        return port;
    }

    public int getConnId() {
        int connId = -1;
        short port = -1;
        String hostname = "www.google.com";
        byte[] header = getHeader();
        ByteBuffer headerBuffer = ByteBuffer.wrap(header);

        connId = headerBuffer.getInt();
        long userFlag = headerBuffer.getLong(4);
        int addrLen = headerBuffer.getInt(4 + 8);
        byte[] addrBytes = new byte[addrLen];

        System.arraycopy(header, 4 + 8 + 4, addrBytes, 0, addrLen);
        try {
            hostname = new String(addrBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        port = headerBuffer.getShort(4 + 8 + 4 + addrLen);
        return connId;
    }

//    public int getLocalConnectionId() {
//        return header.getInt(0);
//    }

    public static byte[] newHeader(String hostName, short port, int localConnId, long userFlag) throws UnsupportedEncodingException {
        //localConnId[4] + userFlag[8] + addrLen[4] + addr + port[2]
        byte[] addrBytes = hostName.getBytes("utf-8");

        byte[] headerBytes = new byte[4 + 8 + 4 + addrBytes.length  + 2];
        ByteBuffer header = ByteBuffer.wrap(headerBytes);

        System.arraycopy(addrBytes, 0, headerBytes, 4 + 8 + 4, addrBytes.length);
        header.putInt(localConnId);
        header.putLong(4, userFlag);
        header.putInt(4 + 8, addrBytes.length);
        header.putShort(4 + 8 + 4 + addrBytes.length, port);
        return headerBytes;
    }
}
