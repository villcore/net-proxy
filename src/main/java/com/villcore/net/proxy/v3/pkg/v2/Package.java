package com.villcore.net.proxy.v3.pkg.v2;

import com.villcore.net.proxy.bio.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * make sure construct Package with exist ByteBuf read/writer index correct
 */
public class Package implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Package.class);

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
        ByteBuffer.wrap(fixed).putInt(4 + 4, body.length);
        setTotolLen();
    }

    public void setTotolLen() {
        int total = getBodyLen() + getHeaderLen() + FIXED_LEN;
        ByteBuffer.wrap(fixed).putInt(total);
    }

    public void setPkgType(short type) {
        ByteBuffer.wrap(fixed).putShort(4 + 4 + 4, type);
    }

    public byte[] toBytes() {
        byte[] all = new byte[FIXED_LEN + getHeaderLen() + getBodyLen()];
        ByteArrayUtils.cpyToNew(fixed, all, 0, 0, fixed.length);
        ByteArrayUtils.cpyToNew(getHeader(), all, 0, fixed.length, getHeaderLen());
        ByteArrayUtils.cpyToNew(getBody(), all, 0, fixed.length + getHeaderLen(), getBodyLen());
        return all;
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

    public Package valueOf(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int totalLen = byteBuffer.getInt();
        int headerLen = byteBuffer.getInt(4);
        int bodyLen = byteBuffer.getInt(4 + 4);
        short pkgType = byteBuffer.getShort(4 + 4 + 4);


//        LOGGER.debug("bytes size = {}, totalLen = {}, headerLen = {}, bodyLen = {}, pkgType = {}", new Object[]{
//                bytes.length, totalLen, headerLen, bodyLen, pkgType
//        });

        byte[] fixed = new byte[FIXED_LEN];
        byte[] header = new byte[headerLen];
        byte[] body = new byte[bodyLen];

        System.arraycopy(bytes, 0, fixed, 0, FIXED_LEN);
        System.arraycopy(bytes, FIXED_LEN, header, 0, headerLen);

        //LOGGER.debug("src pos = {}, dst len = {}", FIXED_LEN + headerLen, body.length);
        System.arraycopy(bytes, FIXED_LEN + headerLen, body, 0, bodyLen);

        setFixed(fixed);
        setHeader(header);
        setBody(body);
        return this;
    }
}
