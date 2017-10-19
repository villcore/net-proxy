package com.villcore.net.proxy.v3.pkg.v2.connection;

import com.villcore.net.proxy.v3.pkg.v2.PackageType;
import com.villcore.net.proxy.v3.pkg.v2.Package;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ConnectAuthReqPackage extends Package {
    {
        this.setPkgType(PackageType.PKG_CONNECTION_AUTH_REQ);
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

        String password = "";
        int passwordLen = headerBuffer.getInt(4 + usernameLen);
        byte[] passwordBytes = new byte[passwordLen];

        System.arraycopy(header, 4 + usernameLen + 4, passwordBytes, 0, passwordLen);
        try {
            password = new String(passwordBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return username;
    }

    public String getPassword() {
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

        String password = "";
        int passwordLen = headerBuffer.getInt(4 + usernameLen);
        byte[] passwordBytes = new byte[passwordLen];

        System.arraycopy(header, 4 + usernameLen + 4, passwordBytes, 0, passwordLen);
        try {
            password = new String(passwordBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return password;
    }

    public static byte[] newHeader(String username, String password) throws UnsupportedEncodingException {
        byte[] usernameBytes = username.getBytes("utf-8");
        byte[] passwordBytes = password.getBytes("utf-8");

        byte[] headerBytes = new byte[4 + usernameBytes.length + 4 + passwordBytes.length];

        System.arraycopy(usernameBytes, 0, headerBytes, 4, usernameBytes.length);
        System.arraycopy(passwordBytes, 0, headerBytes, 4 + usernameBytes.length + 4, passwordBytes.length);

        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        header.putInt(usernameBytes.length);
        header.putInt(4 + usernameBytes.length, passwordBytes.length);
        return headerBytes;
    }
}
