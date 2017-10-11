package com.villcore.net.proxy.v3.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;

public class ConnectReqPackage extends Package {
    {
        this.setPkgType(PackageType.PKG_CONNECT_REQ);
    }

    public String getHostname() {
        String hostname = "";
        ByteBuf header = getHeader();
        int oriRederIndex = header.readerIndex();

        header.readInt();
        header.readLong();
        int hostnameLen = header.readInt();
        byte[] hostnameBytes = new byte[hostnameLen];
        header.readBytes(hostnameBytes);
        header.readerIndex(oriRederIndex);

        try {
            hostname = new String(hostnameBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            header.readerIndex(oriRederIndex);
        }
        return hostname;
    }

    public short getPort() {
        short port = -1;
        ByteBuf header = getHeader();
        int oriRederIndex = header.readerIndex();

        header.readInt();
        header.readLong();
        int hostnameLen = header.readInt();
        byte[] hostnameBytes = new byte[hostnameLen];
        header.readBytes(hostnameBytes);
        port = header.readShort();

        header.readerIndex(oriRederIndex);
        return port;
    }

    public int getConnId() {
        int connId = -1;
        ByteBuf header = getHeader();
        int oriRederIndex = header.readerIndex();

        connId = header.readInt();

        header.readerIndex(oriRederIndex);
        return connId;
    }

//    public int getLocalConnectionId() {
//        return header.getInt(0);
//    }

    public static ByteBuf newHeader(String hostName, short port, int localConnId, long userFlag) throws UnsupportedEncodingException {
        //localConnId[4] + userFlag[8] + addrLen[4] + addr + port[2]
        byte[] addrBytes = hostName.getBytes("utf-8");

        ByteBuf header = Unpooled.buffer(4 + 8 + 4 + addrBytes.length  + 2);

        header.writeInt(localConnId);
        header.writeLong(userFlag);
        header.writeInt(addrBytes.length);
        header.writeBytes(addrBytes);
        header.writeShort(port);
        header.writerIndex(header.capacity());
        return header;
    }

    public static ConnectReqPackage valueOf2(ByteBuf byteBuf) {
        ConnectReqPackage pkg = new ConnectReqPackage();

        int readerIndex = byteBuf.readerIndex();

        int totalLen = byteBuf.getInt(readerIndex);
        int headerLen = byteBuf.getInt(readerIndex + 4);
        int bodyLen = byteBuf.getInt(readerIndex + 4 + 4);
        short pkgType = byteBuf.getShort(readerIndex + 4 + 4 + 4);

        ByteBuf fixed = byteBuf.slice(readerIndex, FIXED_LEN);
        ByteBuf header = byteBuf.slice(readerIndex + FIXED_LEN, headerLen);
        ByteBuf body = byteBuf.slice(readerIndex + FIXED_LEN + headerLen, bodyLen);

        pkg.setFixed(byteBuf.slice(0, FIXED_LEN));
        pkg.setHeader(header);
        pkg.setBody(body);
        return pkg;
    }
}
