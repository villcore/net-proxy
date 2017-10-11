package com.villcore.net.proxy.v3.pkg.v2;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * make sure construct Package with exist ByteBuf read/writer index correct
 */
public class Package implements Serializable {
    public static final int FIXED_LEN = 4 + 4 + 4 + 2;

    protected byte[] fixed = new byte[FIXED_LEN];

    //header
    protected byte[] header = new byte[0];

    //body
    protected byte[] body = new byte[0];

    public int getTotalLen() {
        return ByteBuffer.wrap(fixed).getInt();
    }

    public int getHeaderLen() {
        return ByteBuffer.wrap(fixed).getInt(4);
    }

    public int getBodyLen() {
        return ByteBuffer.wrap(fixed).getInt(4 + 4);
    }

    public short getPkgType() {
        return ByteBuffer.wrap(fixed).getShort(4 + 4 + 4);
    }

    public byte[] getHeader() {
        return this.header;
    }

    public byte[] getFixed() {
        return this.fixed;
    }

    public byte[] getBody() {
        return this.body;
    }

    public void setFixed(byte[] fixed) {
        this.fixed = fixed;
    }

    public void setHeader(byte[] header) {
        this.header = header;
        ByteBuffer.wrap(fixed).putInt(4, header.length);
        setTotolLen();
    }

    public void setBody(byte[] body) {
        this.body = body;
        ByteBuffer.wrap(fixed).putInt(4 + 4, header.length);
        setTotolLen();
    }

    public void setTotolLen() {
        int total = getBodyLen() + getHeaderLen() + FIXED_LEN;
        ByteBuffer.wrap(fixed).putInt(total);
    }

    public void setPkgType(short type) {
        ByteBuffer.wrap(fixed).putInt(4 + 4 + 4, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Package aPackage = (Package) o;

        if (!fixed.equals(aPackage.fixed)) return false;
        if (!header.equals(aPackage.header)) return false;
        return body.equals(aPackage.body);
    }

    @Override
    public int hashCode() {
        int result = fixed.hashCode();
        result = 31 * result + header.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Package{" +
                "fixed=" + fixed +
                ", header=" + header +
                ", body=" + body +
                '}';
    }

    public static Package valueOf(byte[] bytes) {
        Package pkg = new Package();

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int totalLen = byteBuffer.getInt();
        int headerLen = byteBuffer.getInt(4);
        int bodyLen = byteBuffer.getInt(4 + 4);
        short pkgType = byteBuffer.getShort(4 + 4 + 4);

        byte[] fixed = new byte[FIXED_LEN];
        byte[] header = new byte[headerLen];
        byte[] body = new byte[bodyLen];

        System.arraycopy(bytes, 0, fixed, 0, FIXED_LEN);
        System.arraycopy(bytes, FIXED_LEN, header, 0, headerLen);
        System.arraycopy(bytes, FIXED_LEN + headerLen, body, 0, bodyLen);

        pkg.setFixed(fixed);
        pkg.setHeader(header);
        pkg.setBody(body);
        return pkg;
    }
}
