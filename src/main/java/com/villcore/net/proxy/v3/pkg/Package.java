package com.villcore.net.proxy.v3.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.io.Serializable;

/**
 * make sure construct Package with exist ByteBuf read/writer index correct
 */
public class Package implements Serializable {
    //totalLen
    //headerLen
    //bodyLen
    //pkgType

    public static final int FIXED_LEN = 4 + 4 + 4 + 2;

    protected ByteBuf fixed = Unpooled.buffer(FIXED_LEN);

    //header
    protected ByteBuf header = Unpooled.EMPTY_BUFFER;

    //body
    protected ByteBuf body = Unpooled.EMPTY_BUFFER;

    public int getTotalLen() {
        return this.fixed.getInt(0);
    }

    public int getHeaderLen() {
        return this.fixed.getInt(4);
    }

    public int getBodyLen() {
        return this.fixed.getInt(4 + 4);
    }

    public short getPkgType() {
        return this.fixed.getShort(4 + 4 + 4);
    }

    public ByteBuf getHeader() {
        return this.header;
    }

    public ByteBuf getBody() {
        return this.body;
    }

    public void setFixed(ByteBuf fixed) {
        this.fixed = fixed;
    }

    public void setHeader(ByteBuf header) {
        this.header = header;
        this.fixed.setInt(4, header.writerIndex() - header.readerIndex());
        setTotolLen();
    }

    public void setBody(ByteBuf body) {
        this.body = body;
        this.fixed.setInt(4 + 4, body.writerIndex() - body.readerIndex());
        setTotolLen();
    }

    public void setTotolLen() {
        int total = getBodyLen() + getHeaderLen() + FIXED_LEN;
        fixed.setInt(0, total);
        fixed.writerIndex(FIXED_LEN);
    }



    public void setPkgType(short type) {
        this.fixed.setShort(4 + 4 + 4, type);
    }

    public ByteBuf toByteBuf() {
        return Unpooled.wrappedBuffer(fixed, header, body);
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

    public static Package valueOf(ByteBuf byteBuf) {
        Package pkg = new Package();

        ByteBuf fixed = Unpooled.buffer(FIXED_LEN);

        int headerLen = byteBuf.getInt(0);
        int bodyLen = byteBuf.getInt(4);
        short pkgType = byteBuf.getShort(4 + 4);

        fixed.writeInt(headerLen + bodyLen).writeInt(headerLen).writeInt(bodyLen).writeShort(pkgType);

        ByteBuf header = byteBuf.slice(4 + 4 + 2, headerLen);
        header.writerIndex(headerLen);


        ByteBuf body = byteBuf.slice(4 + 4 + 2 + headerLen, bodyLen);
        body.writerIndex(bodyLen);

        pkg.setFixed(fixed);
        pkg.setHeader(header);
        pkg.setBody(body);
        return pkg;
    }
}
