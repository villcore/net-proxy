package com.villcore.net.proxy.v3.pkg.v2.connection;

import com.villcore.net.proxy.v3.pkg.v2.PackageType;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ConnectAuthRespPackage extends Package {
    //authId
    {
        setPkgType(PackageType.PKG_CONNECTION_AUTH_RESP);
    }

    public String getUsername() {
        String username = "";
        byte[] header = getHeader();
        ByteBuffer headerBuffer = ByteBuffer.wrap(header);

        int usernameLen = headerBuffer.getInt();
        byte[] usernameBytes = new byte[usernameLen];

        System.arraycopy(header, 4, usernameBytes, 0, usernameLen);
        try {
            username = new String(usernameBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return username;
    }

    public short getStateCode() {
       short stateCode = -1;
        byte[] header = getHeader();
        ByteBuffer headerBuffer = ByteBuffer.wrap(header);

        int usernameLen = headerBuffer.getInt();
        stateCode = headerBuffer.getShort(4 + usernameLen);
        return stateCode;
    }

    public static byte[] newHeader(String username, short stateCode) throws UnsupportedEncodingException {
        byte[] usernameBytes = username.getBytes("utf-8");
        byte[] headerBytes = new byte[4 + usernameBytes.length + 2];

        System.arraycopy(usernameBytes, 0, headerBytes, 4, usernameBytes.length);

        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        header.putInt(usernameBytes.length);
        header.putShort(4 + usernameBytes.length, stateCode);
        return headerBytes;
    }
}
